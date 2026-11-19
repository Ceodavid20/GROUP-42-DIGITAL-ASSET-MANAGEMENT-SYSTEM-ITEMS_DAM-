package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.AssignmentRequestDto;
import ng.edu.ui.items.dam.dto.AssignmentResponseDto;
import ng.edu.ui.items.dam.dto.ReturnRequestDto;
import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.entity.Assignment;
import ng.edu.ui.items.dam.entity.User;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.exception.InvalidStateException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.AssignmentRepository;
import ng.edu.ui.items.dam.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Section 4.2.3: issue workflow creates current assignment and sets ASSIGNED;
 * return closes it. Single transaction; concurrency-safe via row check.
 * FR-04. Illustrative validation logic (Section 4.2.5).
 */
@Service
public class AssignmentService {

    private final AssetRepository assets;
    private final AssignmentRepository assignments;
    private final UserRepository users;
    private final AuditService audit;

    public AssignmentService(AssetRepository assets, AssignmentRepository assignments,
                             UserRepository users, AuditService audit) {
        this.assets = assets;
        this.assignments = assignments;
        this.users = users;
        this.audit = audit;
    }

    @Transactional
    public AssignmentResponseDto issue(AssignmentRequestDto dto) {
        // (1) actor permission enforced by @PreAuthorize on controller + roles
        Asset asset = assets.findById(dto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + dto.getAssetId()));
        // (2) asset must be AVAILABLE
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new InvalidStateException("Asset " + asset.getAssetTag()
                    + " is not available for issue (current: " + asset.getStatus() + ")");
        }
        // (3) custodian active and valid
        User custodian = users.findById(dto.getCustodianId())
                .orElseThrow(() -> new ResourceNotFoundException("Custodian not found: " + dto.getCustodianId()));
        if (!custodian.isEnabled()) {
            throw new InvalidStateException("Custodian account is disabled");
        }
        // (4) not a duplicate: no active assignment may already exist
        if (assignments.findByAssetIdAndActiveTrue(asset.getId()).isPresent()) {
            throw new InvalidStateException("Asset already has an active assignment");
        }
        User actor = currentUser();

        Assignment a = Assignment.builder()
                .asset(asset)
                .custodian(custodian)
                .assignedBy(actor)
                .assignedOn(LocalDateTime.now())
                .conditionOnIssue(dto.getConditionOnIssue())
                .notes(dto.getNotes())
                .active(true)
                .build();
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setCurrentCustodian(custodian);
        Assignment saved = assignments.save(a);
        audit.record(AuditAction.ASSET_ASSIGNED, "ASSIGNMENT", saved.getId(),
                "Issued " + asset.getAssetTag() + " to " + custodian.getWorkIdentifier());
        return toDto(saved);
    }

    @Transactional
    public AssignmentResponseDto returnAsset(Long assignmentId, ReturnRequestDto dto) {
        Assignment a = assignments.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));
        if (!a.isActive()) {
            throw new InvalidStateException("Assignment is already closed");
        }
        a.setActive(false);
        a.setReturnedOn(LocalDateTime.now());
        a.setConditionOnReturn(dto.getConditionOnReturn());
        if (dto.getNotes() != null && !dto.getNotes().isBlank()) {
            a.setNotes(dto.getNotes());
        }
        Asset asset = a.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setCurrentCustodian(null);
        audit.record(AuditAction.ASSET_RETURNED, "ASSIGNMENT", a.getId(),
                "Returned " + asset.getAssetTag());
        return toDto(a);
    }

    @Transactional(readOnly = true)
    public java.util.List<AssignmentResponseDto> history(Long assetId) {
        return assignments.findByAssetIdOrderByAssignedOnDesc(assetId).stream().map(this::toDto).toList();
    }

    public AssignmentResponseDto toDto(Assignment a) {
        return AssignmentResponseDto.builder()
                .id(a.getId())
                .assetId(a.getAsset().getId())
                .assetTag(a.getAsset().getAssetTag())
                .custodianId(a.getCustodian().getId())
                .custodianName(a.getCustodian().getName())
                .custodianStaffId(a.getCustodian().getWorkIdentifier())
                .assignedByName(a.getAssignedBy() != null ? a.getAssignedBy().getName() : null)
                .assignedOn(a.getAssignedOn())
                .returnedOn(a.getReturnedOn())
                .conditionOnIssue(a.getConditionOnIssue())
                .conditionOnReturn(a.getConditionOnReturn())
                .notes(a.getNotes())
                .active(a.isActive())
                .build();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return users.findByUsername(auth.getName()).orElse(null);
    }
}
