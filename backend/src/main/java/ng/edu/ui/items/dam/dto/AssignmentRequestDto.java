package ng.edu.ui.items.dam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequestDto {
    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Custodian ID is required")
    private Long custodianId;

    private String conditionOnIssue;
    private String notes;
}
