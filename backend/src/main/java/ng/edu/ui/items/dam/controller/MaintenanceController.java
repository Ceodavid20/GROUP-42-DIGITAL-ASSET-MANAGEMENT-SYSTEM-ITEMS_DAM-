package ng.edu.ui.items.dam.controller;

import jakarta.validation.Valid;
import ng.edu.ui.items.dam.dto.MaintenanceCreateDto;
import ng.edu.ui.items.dam.dto.MaintenanceResponseDto;
import ng.edu.ui.items.dam.dto.MaintenanceUpdateDto;
import ng.edu.ui.items.dam.enums.MaintenanceStatus;
import ng.edu.ui.items.dam.service.MaintenanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Table 3.4 maintenance. FR-06.
 */
@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','UNIT_CUSTODIAN')")
    public ResponseEntity<MaintenanceResponseDto> open(@Valid @RequestBody MaintenanceCreateDto dto) {
        MaintenanceResponseDto saved = service.open(dto);
        return ResponseEntity.created(URI.create("/api/v1/maintenance/" + saved.getId())).body(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','MAINTENANCE_TECHNICIAN')")
    public ResponseEntity<MaintenanceResponseDto> update(@PathVariable Long id,
                                                         @RequestBody MaintenanceUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','UNIT_CUSTODIAN','MAINTENANCE_TECHNICIAN','AUDITOR')")
    public ResponseEntity<List<MaintenanceResponseDto>> history(@PathVariable Long assetId) {
        return ResponseEntity.ok(service.history(assetId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','MAINTENANCE_TECHNICIAN','AUDITOR')")
    public ResponseEntity<Page<MaintenanceResponseDto>> byStatus(
            @RequestParam(defaultValue = "OPEN") MaintenanceStatus status, Pageable pageable) {
        return ResponseEntity.ok(service.byStatus(status, pageable));
    }
}
