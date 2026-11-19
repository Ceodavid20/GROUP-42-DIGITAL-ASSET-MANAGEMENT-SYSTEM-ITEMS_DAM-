package ng.edu.ui.items.dam.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentResponseDto {
    private Long id;
    private Long assetId;
    private String assetTag;
    private String fileName;
    private String mimeType;
    private String accessLabel;
    private LocalDateTime uploadedAt;
}
