package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;
import ng.edu.ui.items.dam.enums.TransferDecision;

import java.time.LocalDateTime;

/**
 * Transfer Entity (Table 3.3 Data Dictionary, Figure 3.4 Workflow)
 * id, asset_id, from_id, to_id, decision, approved_by
 */
@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_location_id", nullable = false)
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_location_id", nullable = false)
    private Location toLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_custodian_id")
    private User fromCustodian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_custodian_id")
    private User toCustodian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransferDecision decision = TransferDecision.PENDING;

    @Column(length = 500)
    private String decisionReason;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime decidedAt;
}
