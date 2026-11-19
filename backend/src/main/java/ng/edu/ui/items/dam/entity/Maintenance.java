package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;
import ng.edu.ui.items.dam.enums.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maintenance Entity (Table 3.3 Data Dictionary, Figure 3.2 ERD)
 * id, asset_id, technician_id, issue, status, resolved_at
 */
@Entity
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "technician_id")
    private User technician;

    @Column(nullable = false, length = 1000)
    private String issueDescription;

    @Column(length = 1000)
    private String actionTaken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime openedOn;

    private LocalDateTime resolvedAt;

    private BigDecimal serviceCost;

    @Column(length = 500)
    private String sparePartsUsed;
}
