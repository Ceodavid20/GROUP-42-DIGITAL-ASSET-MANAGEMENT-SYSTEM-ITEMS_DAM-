package ng.edu.ui.items.dam.controller;

import ng.edu.ui.items.dam.entity.AuditEvent;
import ng.edu.ui.items.dam.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FR-09: authorized audit history, read-only.
 */
@RestController
@RequestMapping("/audit-events")
public class AuditController {

    private final AuditEventRepository repo;

    public AuditController(AuditEventRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<List<AuditEvent>> recent() {
        return ResponseEntity.ok(repo.findTop50ByOrderByTimestampDesc());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<Page<AuditEvent>> byEntity(@RequestParam String entityType,
                                                    @RequestParam Long entityId,
                                                    Pageable pageable) {
        return ResponseEntity.ok(repo.findByEntityTypeAndEntityId(entityType, entityId, pageable));
    }
}
