package ng.edu.ui.items.dam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaintenanceCreateDto {
    @NotNull(message = "Asset ID is required")
    private Long assetId;

    private Long technicianId;

    @NotBlank(message = "Defect issue description is required")
    private String issueDescription;
}
