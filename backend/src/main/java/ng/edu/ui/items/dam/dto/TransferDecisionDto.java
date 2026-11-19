package ng.edu.ui.items.dam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ng.edu.ui.items.dam.enums.TransferDecision;

@Data
public class TransferDecisionDto {
    @NotNull(message = "Decision (APPROVED or REJECTED) is required")
    private TransferDecision decision;

    private String decisionReason;
}
