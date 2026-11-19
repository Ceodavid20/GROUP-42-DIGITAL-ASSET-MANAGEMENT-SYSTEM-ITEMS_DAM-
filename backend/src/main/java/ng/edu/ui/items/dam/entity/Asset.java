package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;
import ng.edu.ui.items.dam.enums.AssetStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Asset Entity (Table 3.3 Data Dictionary, Figure 3.2 ERD)
 * id, asset_tag (UNIQUE), serial_no (nullable), brand, model, status, category_id, location_id
 */
@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_asset_tag", columnList = "assetTag", unique = true),
        @Index(name = "idx_asset_status", columnList = "status"),
        @Index(name = "idx_asset_serial", columnList = "serialNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String assetTag; // e.g. "UI-ITEMS-DEMO-001"

    @Column(nullable = false, length = 150)
    private String name; // e.g. "Dell OptiPlex 7090 Micro Desktop"

    @Column(length = 100)
    private String brand; // e.g. "Dell", "Cisco", "HP", "APC"

    @Column(length = 100)
    private String model; // e.g. "OptiPlex 7090", "Catalyst 3850", "Smart-UPS 3000VA"

    @Column(length = 100)
    private String serialNumber; // Nullable as per Section 2.1.3 & Table 3.3

    @Column(length = 50)
    private String macAddress; // e.g. "00:1A:2B:3C:4D:5E"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AssetStatus status = AssetStatus.REGISTERED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_custodian_id")
    private User currentCustodian;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    @Column(length = 500)
    private String specifications; // e.g. "Core i7-11700, 16GB DDR4, 512GB NVMe SSD"

    @Column(length = 1000)
    private String notes;

    @Column(length = 255)
    private String qrCodeData; // e.g. "UI-ITEMS-DEMO-001" for QR label printing

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
