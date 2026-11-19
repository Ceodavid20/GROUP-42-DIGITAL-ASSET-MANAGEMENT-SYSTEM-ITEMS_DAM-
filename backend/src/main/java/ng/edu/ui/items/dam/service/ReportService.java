package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.MaintenanceRepository;
import ng.edu.ui.items.dam.repository.TransferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Section 4.2.4: counts by category/status/unit; aging maintenance;
 * missing metadata; extraction date + filters stated (controller adds them).
 * FR-08, FR-12.
 */
@Service
public class ReportService {

    private final AssetRepository assets;
    private final MaintenanceRepository maintenance;
    private final TransferRepository transfers;

    public ReportService(AssetRepository assets, MaintenanceRepository maintenance, TransferRepository transfers) {
        this.assets = assets;
        this.maintenance = maintenance;
        this.transfers = transfers;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalAssets", assets.count());
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] r : assets.countGroupedByStatus()) {
            byStatus.put(String.valueOf(r[0]), (Long) r[1]);
        }
        m.put("byStatus", byStatus);
        Map<String, Long> byCategory = new HashMap<>();
        for (Object[] r : assets.countGroupedByCategory()) {
            byCategory.put(String.valueOf(r[0]), (Long) r[1]);
        }
        m.put("byCategory", byCategory);
        Map<String, Long> byUnit = new HashMap<>();
        for (Object[] r : assets.countGroupedByUnit()) {
            byUnit.put(String.valueOf(r[0]), (Long) r[1]);
        }
        m.put("byUnit", byUnit);
        m.put("unverifiedRegistered", assets.countUnverifiedRegistered());
        m.put("underMaintenance", assets.countUnderMaintenance());
        m.put("pendingTransfers", transfers.countByDecision(ng.edu.ui.items.dam.enums.TransferDecision.PENDING));
        m.put("openMaintenance", maintenance.countByStatus(ng.edu.ui.items.dam.enums.MaintenanceStatus.OPEN));
        return m;
    }

    @Transactional(readOnly = true)
    public List<Asset> missingMetadata(Pageable pageable) {
        Page<Asset> page = assets.findAll(pageable);
        return page.getContent().stream()
                .filter(a -> a.getSerialNumber() == null || a.getSerialNumber().isBlank()
                        || a.getWarrantyExpiry() == null
                        || a.getSpecifications() == null || a.getSpecifications().isBlank())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Asset> unverified(Pageable pageable) {
        return assets.findByStatus(AssetStatus.REGISTERED, pageable).getContent();
    }
}
