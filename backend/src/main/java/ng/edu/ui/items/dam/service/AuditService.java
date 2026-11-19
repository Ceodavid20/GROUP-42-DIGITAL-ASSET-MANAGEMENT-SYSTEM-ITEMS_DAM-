package ng.edu.ui.items.dam.service;

import jakarta.servlet.http.HttpServletRequest;
import ng.edu.ui.items.dam.entity.AuditEvent;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.repository.AuditEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Append-oriented immutable-style audit trail (Table 3.3).
 * Never stores credentials; minimizes personal data.
 *
 * Actor identity + client IP are captured synchronously on the calling
 * thread (security/request context is not visible to the async writer).
 */
@Service
public class AuditService {

    private final AuditEventRepository repo;

    public AuditService(AuditEventRepository repo) {
        this.repo = repo;
    }

    public void record(AuditAction action, String entityType, Long entityId, String details) {
        String actorName = null;
        String actorRole = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
                actorName = auth.getName();
                if (!auth.getAuthorities().isEmpty()) {
                    actorRole = auth.getAuthorities().iterator().next().getAuthority();
                }
            }
        } catch (Exception ignored) {}
        String ip = null;
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ip = req.getHeader("X-Forwarded-For");
                if (ip == null) ip = req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        persistAsync(action, entityType, entityId, details, actorName, actorRole, ip);
    }

    public void recordSync(AuditAction action, String entityType, Long entityId, String details) {
        record(action, entityType, entityId, details);
    }

    @Async
    public void persistAsync(AuditAction action, String entityType, Long entityId,
                             String details, String actorName, String actorRole, String ip) {
        try {
            AuditEvent e = AuditEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .actorName(actorName)
                    .actorRole(actorRole)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details != null && details.length() > 1500 ? details.substring(0, 1500) : details)
                    .clientIp(ip)
                    .build();
            repo.save(e);
        } catch (Exception ignored) {
            // audit must never break the business transaction
        }
    }
}
