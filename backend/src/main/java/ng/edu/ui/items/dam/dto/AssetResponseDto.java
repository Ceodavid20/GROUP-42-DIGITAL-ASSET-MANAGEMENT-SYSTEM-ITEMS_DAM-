package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;
import ng.edu.ui.items.dam.enums.AssetStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AssetResponseDto {
    private Long id;
    private String assetTag;
    private String name;
    private String brand;
    private String model;
    private String serialNumber;
    private String macAddress;
    private AssetStatus status;
    private Long categoryId;
    private String categoryName;
    private Long locationId;
    private String locationUnit;
    private String locationBuilding;
    private String locationRoom;
    private Long custodianId;
    private String custodianName;
    private String custodianStaffId;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiry;
    private String specifications;
    private String notes;
    private String qrCodeData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
