package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Attachment Entity (Table 3.3 Data Dictionary)
 * id, asset_id, object_path, mime_type, access_label
 * Restricted evidence references (e.g. warranty, disposal/sanitization signoff).
 */
@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String objectPath;

    @Column(length = 100)
    private String mimeType;

    @Column(length = 100)
    private String accessLabel; // e.g. "WARRANTY_DOC", "SANITIZATION_CERTIFICATE", "PURCHASE_RECEIPT"

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
