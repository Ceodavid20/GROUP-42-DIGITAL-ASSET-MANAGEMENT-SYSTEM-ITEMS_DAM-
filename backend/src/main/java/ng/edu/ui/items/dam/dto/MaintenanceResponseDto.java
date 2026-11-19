package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;
import ng.edu.ui.items.dam.enums.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceResponseDto {
    private Long id;
    private Long assetId;
    private String assetTag;
    private Long reportedById;
    private String reportedByName;
    private Long technicianId;
    private String technicianName;
    private String issueDescription;
    private String actionTaken;
    private MaintenanceStatus status;
    private LocalDateTime openedOn;
    private LocalDateTime resolvedAt;
    private BigDecimal serviceCost;
    private String sparePartsUsed;
}
