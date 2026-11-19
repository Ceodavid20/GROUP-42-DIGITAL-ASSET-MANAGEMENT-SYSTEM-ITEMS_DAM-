package ng.edu.ui.items.dam.controller;

import ng.edu.ui.items.dam.service.ReportService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * FR-08, FR-12: every report states extraction date + applied filters.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> m = new HashMap<>(reports.summary());
        m.put("extractionDate", LocalDate.now().toString());
        m.put("filters", "none (full register)");
        return ResponseEntity.ok(m);
    }

    @GetMapping("/summary/unit")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','UNIT_CUSTODIAN','AUDITOR')")
    public ResponseEntity<Map<String, Object>> byUnit() {
        Map<String, Object> full = reports.summary();
        Map<String, Object> m = new HashMap<>();
        m.put("extractionDate", LocalDate.now().toString());
        m.put("filters", "none (full register)");
        m.put("byUnit", full.get("byUnit"));
        m.put("totalAssets", full.get("totalAssets"));
        return ResponseEntity.ok(m);
    }

    @GetMapping("/exceptions/missing-metadata")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<Map<String, Object>> missingMetadata(Pageable pageable) {
        Map<String, Object> m = new HashMap<>();
        m.put("extractionDate", LocalDate.now().toString());
        m.put("filters", "missing serial / warranty / specifications");
        m.put("records", reports.missingMetadata(pageable));
        return ResponseEntity.ok(m);
    }

    @GetMapping("/exceptions/unverified")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER','AUDITOR')")
    public ResponseEntity<Map<String, Object>> unverified(Pageable pageable) {
        Map<String, Object> m = new HashMap<>();
        m.put("extractionDate", LocalDate.now().toString());
        m.put("filters", "status=REGISTERED");
        m.put("records", reports.unverified(pageable));
        return ResponseEntity.ok(m);
    }
}
