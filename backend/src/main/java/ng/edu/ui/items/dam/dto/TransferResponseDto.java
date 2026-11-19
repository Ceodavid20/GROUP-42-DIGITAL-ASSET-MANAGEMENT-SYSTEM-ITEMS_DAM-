package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;
import ng.edu.ui.items.dam.enums.TransferDecision;

import java.time.LocalDateTime;

/**
 * Nested asset/from/to shapes match the Angular client; flat names aid reports.
 */
@Data
@Builder
public class TransferResponseDto {
    private Long id;
    private AssetRef asset;
    private LocationRef fromLocation;
    private LocationRef toLocation;
    private String fromCustodianName;
    private String toCustodianName;
    private String requestedByName;
    private String approvedByName;
    private String reason;
    private TransferDecision decision;
    private String decisionReason;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;

    @Data
    @Builder
    public static class AssetRef {
        private Long id;
        private String assetTag;
    }

    @Data
    @Builder
    public static class LocationRef {
        private Long id;
        private String unitName;
    }
}
