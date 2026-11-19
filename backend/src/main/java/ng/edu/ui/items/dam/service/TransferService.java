package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.TransferDecisionDto;
import ng.edu.ui.items.dam.dto.TransferRequestDto;
import ng.edu.ui.items.dam.dto.TransferResponseDto;
import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.entity.Location;
import ng.edu.ui.items.dam.entity.Transfer;
import ng.edu.ui.items.dam.entity.User;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.enums.TransferDecision;
import ng.edu.ui.items.dam.exception.InvalidStateException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.LocationRepository;
import ng.edu.ui.items.dam.repository.TransferRepository;
import ng.edu.ui.items.dam.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Section 3.4.2 / Figure 3.4: request recorded separately; only an approved
 * decision updates the active location — atomically, with audit. FR-05.
 * Idempotent: deciding an already-decided request returns it unchanged.
 */
@Service
public class TransferService {

    private final TransferRepository transfers;
    private final AssetRepository assets;
    private final LocationRepository locations;
    private final UserRepository users;
    private final AuditService audit;

    public TransferService(TransferRepository transfers, AssetRepository assets,
                           LocationRepository locations, UserRepository users, AuditService audit) {
        this.transfers = transfers;
        this.assets = assets;
        this.locations = locations;
        this.users = users;
        this.audit = audit;
    }

    @Transactional
    public TransferResponseDto request(TransferRequestDto dto) {
        Asset asset = assets.findById(dto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + dto.getAssetId()));
        Location to = locations.findById(dto.getToLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination location not found"));
        if (asset.getLocation() != null && asset.getLocation().getId().equals(to.getId())) {
            throw new InvalidStateException("Asset is already at the destination location");
        }
        switch (asset.getStatus()) {
            case DISPOSED, RETIRED, UNDER_MAINTENANCE -> throw new InvalidStateException(
                    "Asset " + asset.getAssetTag() + " in state " + asset.getStatus() + " cannot be transferred");
            default -> {}
        }
        User toCustodian = null;
        if (dto.getToCustodianId() != null) {
            toCustodian = users.findById(dto.getToCustodianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination custodian not found"));
        }
        User requester = currentUser();

        Transfer t = Transfer.builder()
                .asset(asset)
                .fromLocation(asset.getLocation())
                .toLocation(to)
                .fromCustodian(asset.getCurrentCustodian())
                .toCustodian(toCustodian)
                .requestedBy(requester)
                .reason(dto.getReason())
                .decision(TransferDecision.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        Transfer saved = transfers.save(t);
        audit.record(AuditAction.TRANSFER_REQUESTED, "TRANSFER", saved.getId(),
                "Transfer requested for " + asset.getAssetTag() + " to " + to.getUnitName());
        return toDto(saved);
    }

    @Transactional
    public TransferResponseDto decide(Long id, TransferDecisionDto dto) {
        Transfer t = transfers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found: " + id));
        if (t.getDecision() != TransferDecision.PENDING) {
            return toDto(t); // idempotent: repeat clicks do not duplicate movements
        }
        User approver = currentUser();
        t.setApprovedBy(approver);
        t.setDecidedAt(LocalDateTime.now());
        t.setDecision(dto.getDecision());
        t.setDecisionReason(dto.getDecisionReason());

        if (dto.getDecision() == TransferDecision.APPROVED) {
            Asset asset = t.getAsset();
            asset.setLocation(t.getToLocation());
            if (t.getToCustodian() != null) {
                asset.setCurrentCustodian(t.getToCustodian());
            }
            audit.record(AuditAction.TRANSFER_APPROVED, "TRANSFER", t.getId(),
                    "Transfer approved for " + asset.getAssetTag());
        } else {
            audit.record(AuditAction.TRANSFER_REJECTED, "TRANSFER", t.getId(),
                    "Transfer rejected for " + t.getAsset().getAssetTag());
        }
        return toDto(t);
    }

    @Transactional(readOnly = true)
    public java.util.List<TransferResponseDto> history(Long assetId) {
        return transfers.findByAssetIdOrderByRequestedAtDesc(assetId).stream().map(this::toDto).toList();
    }

    public TransferResponseDto toDto(Transfer t) {
        return TransferResponseDto.builder()
                .id(t.getId())
                .asset(t.getAsset() == null ? null : TransferResponseDto.AssetRef.builder()
                        .id(t.getAsset().getId()).assetTag(t.getAsset().getAssetTag()).build())
                .fromLocation(t.getFromLocation() == null ? null : TransferResponseDto.LocationRef.builder()
                        .id(t.getFromLocation().getId()).unitName(t.getFromLocation().getUnitName()).build())
                .toLocation(t.getToLocation() == null ? null : TransferResponseDto.LocationRef.builder()
                        .id(t.getToLocation().getId()).unitName(t.getToLocation().getUnitName()).build())
                .fromCustodianName(t.getFromCustodian() != null ? t.getFromCustodian().getName() : null)
                .toCustodianName(t.getToCustodian() != null ? t.getToCustodian().getName() : null)
                .requestedByName(t.getRequestedBy() != null ? t.getRequestedBy().getName() : null)
                .approvedByName(t.getApprovedBy() != null ? t.getApprovedBy().getName() : null)
                .reason(t.getReason())
                .decision(t.getDecision())
                .decisionReason(t.getDecisionReason())
                .requestedAt(t.getRequestedAt())
                .decidedAt(t.getDecidedAt())
                .build();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return users.findByUsername(auth.getName()).orElse(null);
    }
}
