package ng.edu.ui.items.dam.controller;

import jakarta.validation.Valid;
import ng.edu.ui.items.dam.dto.AssignmentRequestDto;
import ng.edu.ui.items.dam.dto.AssignmentResponseDto;
import ng.edu.ui.items.dam.dto.ReturnRequestDto;
import ng.edu.ui.items.dam.repository.AssignmentRepository;
import ng.edu.ui.items.dam.service.AssignmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Table 3.4 assignments. FR-04.
 */
@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService service;
    private final AssignmentRepository repo;

    public AssignmentController(AssignmentService service, AssignmentRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssignmentResponseDto> issue(@Valid @RequestBody AssignmentRequestDto dto) {
        AssignmentResponseDto saved = service.issue(dto);
        return ResponseEntity.created(URI.create("/api/v1/assignments/" + saved.getId())).body(saved);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<AssignmentResponseDto> close(@PathVariable Long id, @RequestBody ReturnRequestDto dto) {
        return ResponseEntity.ok(service.returnAsset(id, dto));
    }

    @GetMapping("/asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<List<AssignmentResponseDto>> history(@PathVariable Long assetId) {
        return ResponseEntity.ok(service.history(assetId));
    }

    @GetMapping("/custodian/{custodianId}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<Page<AssignmentResponseDto>> byCustodian(@PathVariable Long custodianId, Pageable pageable) {
        return ResponseEntity.ok(repo.findByCustodianId(custodianId, pageable).map(service::toDto));
    }
}
