package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findTop50ByOrderByTimestampDesc();
    Page<AuditEvent> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
}
