package ng.edu.ui.items.dam.dto;

import lombok.Data;
import ng.edu.ui.items.dam.enums.MaintenanceStatus;

import java.math.BigDecimal;

@Data
public class MaintenanceUpdateDto {
    private MaintenanceStatus status;
    private String actionTaken;
    private BigDecimal serviceCost;
    private String sparePartsUsed;
    private Long technicianId;
}
