package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.AttachmentResponseDto;
import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.entity.Attachment;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.exception.InvalidStateException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Table 3.3 attachments: restricted evidence references
 * (warranty, disposal/sanitization signoff). Files land in the
 * configured vault directory; the DB stores the object path + MIME +
 * access label. No Tika/media pipeline — ICT equipment records (Ch.1).
 */
@Service
public class AttachmentService {

    private final AttachmentRepository repo;
    private final AssetRepository assets;
    private final AuditService audit;
    private final Path uploadDir;

    public AttachmentService(AttachmentRepository repo, AssetRepository assets, AuditService audit,
                             @Value("${dam.storage.upload-dir:./data/storage/assets}") String uploadDir) {
        this.repo = repo;
        this.assets = assets;
        this.audit = audit;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public AttachmentResponseDto store(Long assetId, MultipartFile file, String accessLabel) throws IOException {
        Asset asset = assets.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));
        if (file == null || file.isEmpty()) {
            throw new InvalidStateException("No file supplied");
        }
        Files.createDirectories(uploadDir);
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename());
        if (original.contains("..")) {
            throw new InvalidStateException("Invalid file name: " + original);
        }
        String stored = asset.getAssetTag() + "_" + UUID.randomUUID() + "_" + original;
        Path target = uploadDir.resolve(stored);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Attachment a = Attachment.builder()
                .asset(asset)
                .fileName(original)
                .objectPath(target.toString())
                .mimeType(file.getContentType())
                .accessLabel(accessLabel == null || accessLabel.isBlank() ? "GENERAL" : accessLabel.trim().toUpperCase())
                .build();
        Attachment saved = repo.save(a);
        audit.record(AuditAction.ASSET_REGISTERED, "ATTACHMENT", saved.getId(),
                "Evidence attached to " + asset.getAssetTag() + ": " + original);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> forAsset(Long assetId) {
        assets.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));
        return repo.findByAssetId(assetId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Resource load(Long id) throws MalformedURLException {
        Attachment a = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));
        return new UrlResource(Paths.get(a.getObjectPath()).toUri());
    }

    @Transactional(readOnly = true)
    public Attachment get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));
    }

    private AttachmentResponseDto toDto(Attachment a) {
        return AttachmentResponseDto.builder()
                .id(a.getId())
                .assetId(a.getAsset().getId())
                .assetTag(a.getAsset().getAssetTag())
                .fileName(a.getFileName())
                .mimeType(a.getMimeType())
                .accessLabel(a.getAccessLabel())
                .uploadedAt(a.getUploadedAt())
                .build();
    }
}
