package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponseDto {
    private Long id;
    private Long assetId;
    private String assetTag;
    private Long custodianId;
    private String custodianName;
    private String custodianStaffId;
    private String assignedByName;
    private LocalDateTime assignedOn;
    private LocalDateTime returnedOn;
    private String conditionOnIssue;
    private String conditionOnReturn;
    private String notes;
    private boolean active;
}
