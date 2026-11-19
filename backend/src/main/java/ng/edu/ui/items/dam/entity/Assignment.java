package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Assignment Entity (Table 3.3 Data Dictionary, Figure 3.2 ERD)
 * id, asset_id, custodian_id, assigned_on, returned_on
 * Preserves historical custody trace.
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "custodian_id", nullable = false)
    private User custodian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedOn;

    private LocalDateTime returnedOn;

    @Column(length = 255)
    private String conditionOnIssue;

    @Column(length = 255)
    private String conditionOnReturn;

    @Column(length = 500)
    private String notes;

    @Builder.Default
    private boolean active = true;
}
