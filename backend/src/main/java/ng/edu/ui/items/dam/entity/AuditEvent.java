package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;
import ng.edu.ui.items.dam.enums.AuditAction;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AuditEvent Entity (Table 3.3 Data Dictionary, Figure 3.2 ERD)
 * id, event_id, actor_id, entity_type, entity_id, action, at
 * Append-oriented immutable-style audit trail.
 */
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_entity", columnList = "entityType,entityId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventId; // UUID

    private Long actorId;

    @Column(length = 100)
    private String actorName;

    @Column(length = 50)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @Column(nullable = false, length = 50)
    private String entityType; // "ASSET", "ASSIGNMENT", "TRANSFER", "MAINTENANCE"

    private Long entityId;

    @Column(length = 1500)
    private String details;

    @Column(length = 50)
    private String clientIp;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
