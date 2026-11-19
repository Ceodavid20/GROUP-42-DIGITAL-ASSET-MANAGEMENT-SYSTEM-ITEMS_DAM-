package ng.edu.ui.items.dam.controller;

import jakarta.validation.Valid;
import ng.edu.ui.items.dam.dto.TransferDecisionDto;
import ng.edu.ui.items.dam.dto.TransferRequestDto;
import ng.edu.ui.items.dam.dto.TransferResponseDto;
import ng.edu.ui.items.dam.enums.TransferDecision;
import ng.edu.ui.items.dam.repository.TransferRepository;
import ng.edu.ui.items.dam.service.TransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Table 3.4 transfers. FR-05.
 */
@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService service;
    private final TransferRepository repo;

    public TransferController(TransferService service, TransferRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','UNIT_CUSTODIAN')")
    public ResponseEntity<TransferResponseDto> request(@Valid @RequestBody TransferRequestDto dto) {
        TransferResponseDto saved = service.request(dto);
        return ResponseEntity.created(URI.create("/api/v1/transfers/" + saved.getId())).body(saved);
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<TransferResponseDto> decide(@PathVariable Long id, @Valid @RequestBody TransferDecisionDto dto) {
        return ResponseEntity.ok(service.decide(id, dto));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<Page<TransferResponseDto>> pending(Pageable pageable) {
        return ResponseEntity.ok(repo.findByDecision(TransferDecision.PENDING, pageable).map(service::toDto));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<List<TransferResponseDto>> byAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(service.history(assetId));
    }
}
