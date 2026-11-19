package ng.edu.ui.items.dam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequestDto {
    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Destination Location ID is required")
    private Long toLocationId;

    private Long toCustodianId;

    @NotBlank(message = "Transfer justification reason is required")
    private String reason;
}
