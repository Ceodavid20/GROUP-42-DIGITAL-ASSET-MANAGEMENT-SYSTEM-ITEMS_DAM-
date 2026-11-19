package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.MaintenanceCreateDto;
import ng.edu.ui.items.dam.dto.MaintenanceResponseDto;
import ng.edu.ui.items.dam.dto.MaintenanceUpdateDto;
import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.entity.Maintenance;
import ng.edu.ui.items.dam.entity.User;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.enums.MaintenanceStatus;
import ng.edu.ui.items.dam.exception.InvalidStateException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.MaintenanceRepository;
import ng.edu.ui.items.dam.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Section 4.2.3: case links symptoms, technician, timestamps, outcome.
 * Open maintenance blocks incompatible assignment/disposal. FR-06.
 */
@Service
public class MaintenanceService {

    private final MaintenanceRepository repo;
    private final AssetRepository assets;
    private final UserRepository users;
    private final AuditService audit;

    public MaintenanceService(MaintenanceRepository repo, AssetRepository assets,
                              UserRepository users, AuditService audit) {
        this.repo = repo;
        this.assets = assets;
        this.users = users;
        this.audit = audit;
    }

    @Transactional
    public MaintenanceResponseDto open(MaintenanceCreateDto dto) {
        Asset asset = assets.findById(dto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + dto.getAssetId()));
        if (asset.getStatus() == AssetStatus.DISPOSED || asset.getStatus() == AssetStatus.RETIRED) {
            throw new InvalidStateException("Cannot open maintenance on " + asset.getStatus() + " asset");
        }
        if (asset.getStatus() == AssetStatus.UNDER_MAINTENANCE) {
            throw new InvalidStateException("Asset already has an open maintenance case");
        }
        User reporter = currentUser();
        User tech = dto.getTechnicianId() == null ? null : users.findById(dto.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        Maintenance m = Maintenance.builder()
                .asset(asset)
                .reportedBy(reporter)
                .technician(tech)
                .issueDescription(dto.getIssueDescription())
                .status(MaintenanceStatus.OPEN)
                .openedOn(LocalDateTime.now())
                .build();
        asset.setStatus(AssetStatus.UNDER_MAINTENANCE);
        Maintenance saved = repo.save(m);
        audit.record(AuditAction.MAINTENANCE_OPENED, "MAINTENANCE", saved.getId(),
                "Maintenance opened for " + asset.getAssetTag());
        return toDto(saved);
    }

    @Transactional
    public MaintenanceResponseDto update(Long id, MaintenanceUpdateDto dto) {
        Maintenance m = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance case not found: " + id));
        if (dto.getTechnicianId() != null) {
            m.setTechnician(users.findById(dto.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found")));
        }
        if (dto.getStatus() != null) {
            if (m.getStatus() == MaintenanceStatus.RESOLVED || m.getStatus() == MaintenanceStatus.CANCELLED) {
                throw new InvalidStateException("Closed maintenance cases cannot be reopened via update");
            }
            m.setStatus(dto.getStatus());
            if (dto.getStatus() == MaintenanceStatus.RESOLVED) {
                m.setResolvedAt(LocalDateTime.now());
                Asset asset = m.getAsset();
                asset.setStatus(asset.getCurrentCustodian() != null ? AssetStatus.ASSIGNED : AssetStatus.AVAILABLE);
                audit.record(AuditAction.MAINTENANCE_RESOLVED, "MAINTENANCE", m.getId(),
                        "Maintenance resolved for " + asset.getAssetTag());
            } else {
                audit.record(AuditAction.MAINTENANCE_UPDATED, "MAINTENANCE", m.getId(),
                        "Maintenance updated to " + dto.getStatus());
            }
        }
        if (dto.getActionTaken() != null) m.setActionTaken(dto.getActionTaken());
        if (dto.getServiceCost() != null) m.setServiceCost(dto.getServiceCost());
        if (dto.getSparePartsUsed() != null) m.setSparePartsUsed(dto.getSparePartsUsed());
        return toDto(m);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponseDto> history(Long assetId) {
        return repo.findByAssetIdOrderByOpenedOnDesc(assetId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceResponseDto> byStatus(MaintenanceStatus status, Pageable pageable) {
        return repo.findByStatus(status, pageable).map(this::toDto);
    }

    MaintenanceResponseDto toDto(Maintenance m) {
        return MaintenanceResponseDto.builder()
                .id(m.getId())
                .assetId(m.getAsset().getId())
                .assetTag(m.getAsset().getAssetTag())
                .reportedById(m.getReportedBy() != null ? m.getReportedBy().getId() : null)
                .reportedByName(m.getReportedBy() != null ? m.getReportedBy().getName() : null)
                .technicianId(m.getTechnician() != null ? m.getTechnician().getId() : null)
                .technicianName(m.getTechnician() != null ? m.getTechnician().getName() : null)
                .issueDescription(m.getIssueDescription())
                .actionTaken(m.getActionTaken())
                .status(m.getStatus())
                .openedOn(m.getOpenedOn())
                .resolvedAt(m.getResolvedAt())
                .serviceCost(m.getServiceCost())
                .sparePartsUsed(m.getSparePartsUsed())
                .build();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return users.findByUsername(auth.getName()).orElse(null);
    }
}
