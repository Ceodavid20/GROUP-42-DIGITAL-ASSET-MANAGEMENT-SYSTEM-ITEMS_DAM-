package ng.edu.ui.items.dam.controller;

import jakarta.validation.Valid;
import ng.edu.ui.items.dam.dto.AssetCreateDto;
import ng.edu.ui.items.dam.dto.AssetResponseDto;
import ng.edu.ui.items.dam.dto.AssetSearchCriteria;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.service.AssetService;
import ng.edu.ui.items.dam.service.RetirementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

/**
 * Table 3.4 assets + retirement (FR-02, FR-03, FR-07, FR-11 via tag lookup).
 */
@RestController
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assets;
    private final RetirementService retirement;

    public AssetController(AssetService assets, RetirementService retirement) {
        this.assets = assets;
        this.retirement = retirement;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssetResponseDto> register(@Valid @RequestBody AssetCreateDto dto) {
        AssetResponseDto saved = assets.register(dto);
        return ResponseEntity.created(URI.create("/api/v1/assets/" + saved.getId())).body(saved);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','UNIT_CUSTODIAN','MAINTENANCE_TECHNICIAN','AUDITOR')")
    public ResponseEntity<Page<AssetResponseDto>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String unitName,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String serialNumber,
            Pageable pageable) {
        AssetSearchCriteria c = new AssetSearchCriteria();
        c.setQuery(query);
        c.setCategoryId(categoryId);
        c.setLocationId(locationId);
        c.setUnitName(unitName);
        c.setStatus(status);
        c.setSerialNumber(serialNumber);
        return ResponseEntity.ok(assets.search(c, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','UNIT_CUSTODIAN','MAINTENANCE_TECHNICIAN','AUDITOR')")
    public ResponseEntity<AssetResponseDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(assets.getDto(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssetResponseDto> update(@PathVariable Long id, @Valid @RequestBody AssetCreateDto dto) {
        return ResponseEntity.ok(assets.update(id, dto));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssetResponseDto> verify(@PathVariable Long id) {
        return ResponseEntity.ok(assets.verify(id));
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssetResponseDto> retire(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(assets.toDto(
                retirement.retire(id, body.getOrDefault("reason", "No reason supplied"))));
    }

    @PostMapping("/{id}/dispose")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssetResponseDto> dispose(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(assets.toDto(
                retirement.dispose(id, body.getOrDefault("evidence", "No evidence supplied"))));
    }
}
