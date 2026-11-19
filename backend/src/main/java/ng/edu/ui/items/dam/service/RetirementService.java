package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.exception.InvalidStateException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-07: retirement requires approval, reason, evidence note; disposal only
 * from RETIRED after return/reconciliation. Data-bearing equipment needs
 * authorized sanitization evidence (NIST SP 800-88r2) — recorded, not performed.
 */
@Service
public class RetirementService {

    private final AssetRepository assets;
    private final AuditService audit;

    public RetirementService(AssetRepository assets, AuditService audit) {
        this.assets = assets;
        this.audit = audit;
    }

    @Transactional
    public Asset retire(Long assetId, String reason) {
        Asset a = assets.findById(assetId)
                .orElseThrow(() -> new ng.edu.ui.items.dam.exception.ResourceNotFoundException("Asset not found: " + assetId));
        if (a.getStatus() == AssetStatus.ASSIGNED) {
            throw new InvalidStateException("Asset must be returned before retirement");
        }
        if (a.getStatus() == AssetStatus.UNDER_MAINTENANCE) {
            throw new InvalidStateException("Asset under maintenance cannot be retired");
        }
        if (a.getStatus() == AssetStatus.DISPOSED || a.getStatus() == AssetStatus.RETIRED) {
            throw new InvalidStateException("Asset is already " + a.getStatus());
        }
        a.setStatus(AssetStatus.RETIRED);
        a.setNotes(appendNote(a.getNotes(), "RETIREMENT: " + reason));
        audit.record(AuditAction.ASSET_RETIRED, "ASSET", a.getId(), "Retired " + a.getAssetTag() + ": " + reason);
        return a;
    }

    @Transactional
    public Asset dispose(Long assetId, String evidence) {
        Asset a = assets.findById(assetId)
                .orElseThrow(() -> new ng.edu.ui.items.dam.exception.ResourceNotFoundException("Asset not found: " + assetId));
        if (a.getStatus() != AssetStatus.RETIRED) {
            throw new InvalidStateException("Only RETIRED assets can be disposed; current: " + a.getStatus());
        }
        a.setStatus(AssetStatus.DISPOSED);
        a.setNotes(appendNote(a.getNotes(), "DISPOSAL/SANITIZATION EVIDENCE: " + evidence));
        audit.record(AuditAction.ASSET_DISPOSED, "ASSET", a.getId(), "Disposed " + a.getAssetTag() + ": " + evidence);
        return a;
    }

    private String appendNote(String existing, String addition) {
        if (existing == null || existing.isBlank()) return addition;
        return existing + "\n" + addition;
    }
}
