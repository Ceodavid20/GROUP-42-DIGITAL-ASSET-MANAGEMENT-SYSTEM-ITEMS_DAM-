package ng.edu.ui.items.dam.controller;

import ng.edu.ui.items.dam.dto.AttachmentResponseDto;
import ng.edu.ui.items.dam.entity.Attachment;
import ng.edu.ui.items.dam.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Restricted evidence references (Table 3.3). Read/download limited to
 * ADMIN / ASSET_OFFICER / AUDITOR (SecurityConfig); custodians and
 * technicians get no access — enforced server-side.
 */
@RestController
@RequestMapping("/attachments")
@PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AttachmentResponseDto> upload(@RequestParam Long assetId,
                                                       @RequestParam MultipartFile file,
                                                       @RequestParam(required = false) String accessLabel)
            throws Exception {
        return ResponseEntity.ok(service.store(assetId, file, accessLabel));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<AttachmentResponseDto>> forAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(service.forAsset(assetId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {
        Attachment a = service.get(id);
        Resource r = service.load(id);
        String filename = URLEncoder.encode(a.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getMimeType() == null ? "application/octet-stream" : a.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(r);
    }
}
