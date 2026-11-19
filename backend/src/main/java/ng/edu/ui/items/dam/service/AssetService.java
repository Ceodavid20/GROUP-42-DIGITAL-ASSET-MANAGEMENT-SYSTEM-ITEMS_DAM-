package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.AssetCreateDto;
import ng.edu.ui.items.dam.dto.AssetResponseDto;
import ng.edu.ui.items.dam.dto.AssetSearchCriteria;
import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.entity.Category;
import ng.edu.ui.items.dam.entity.Location;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.enums.AuditAction;
import ng.edu.ui.items.dam.exception.DuplicateResourceException;
import ng.edu.ui.items.dam.exception.InvalidStateException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.CategoryRepository;
import ng.edu.ui.items.dam.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 4.2.2: catalogue + master data. Normalizes identifiers,
 * prevents duplicate tags, guards state edits. FR-02, FR-03.
 */
@Service
public class AssetService {

    private final AssetRepository assets;
    private final CategoryRepository categories;
    private final LocationRepository locations;
    private final AuditService audit;

    public AssetService(AssetRepository assets, CategoryRepository categories,
                        LocationRepository locations, AuditService audit) {
        this.assets = assets;
        this.categories = categories;
        this.locations = locations;
        this.audit = audit;
    }

    @Transactional
    public AssetResponseDto register(AssetCreateDto dto) {
        String tag = dto.getAssetTag() == null ? null : dto.getAssetTag().trim().toUpperCase();
        if (tag == null || tag.isEmpty()) {
            throw new InvalidStateException("Asset tag is required");
        }
        if (assets.existsByAssetTag(tag)) {
            throw new DuplicateResourceException("Asset tag already exists: " + tag);
        }
        Category cat = categories.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));
        Location loc = locations.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + dto.getLocationId()));

        Asset asset = Asset.builder()
                .assetTag(tag)
                .name(dto.getName().trim())
                .brand(trimOrNull(dto.getBrand()))
                .model(trimOrNull(dto.getModel()))
                .serialNumber(trimOrNull(dto.getSerialNumber()))
                .macAddress(dto.getMacAddress() == null ? null : dto.getMacAddress().trim())
                .status(AssetStatus.REGISTERED)
                .category(cat)
                .location(loc)
                .purchaseDate(dto.getPurchaseDate())
                .warrantyExpiry(dto.getWarrantyExpiry())
                .specifications(trimOrNull(dto.getSpecifications()))
                .notes(trimOrNull(dto.getNotes()))
                .qrCodeData(tag)
                .build();
        Asset saved = assets.save(asset);
        audit.record(AuditAction.ASSET_REGISTERED, "ASSET", saved.getId(),
                "Registered asset " + saved.getAssetTag());
        return toDto(saved);
    }

    @Transactional
    public AssetResponseDto verify(Long id) {
        Asset a = get(id);
        if (a.getStatus() != AssetStatus.REGISTERED) {
            throw new InvalidStateException("Only REGISTERED assets can be verified; current: " + a.getStatus());
        }
        a.setStatus(AssetStatus.AVAILABLE);
        audit.record(AuditAction.ASSET_VERIFIED, "ASSET", id, "Verified " + a.getAssetTag());
        return toDto(a);
    }

    @Transactional
    public AssetResponseDto update(Long id, AssetCreateDto dto) {
        Asset a = get(id);
        if (a.getStatus() == AssetStatus.DISPOSED) {
            throw new InvalidStateException("Disposed assets cannot be amended");
        }
        Category cat = categories.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Location loc = locations.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        a.setName(dto.getName().trim());
        a.setBrand(trimOrNull(dto.getBrand()));
        a.setModel(trimOrNull(dto.getModel()));
        a.setSerialNumber(trimOrNull(dto.getSerialNumber()));
        a.setMacAddress(dto.getMacAddress() == null ? null : dto.getMacAddress().trim());
        a.setCategory(cat);
        if (a.getStatus() != AssetStatus.ASSIGNED) {
            a.setLocation(loc);
        }
        a.setPurchaseDate(dto.getPurchaseDate());
        a.setWarrantyExpiry(dto.getWarrantyExpiry());
        a.setSpecifications(trimOrNull(dto.getSpecifications()));
        a.setNotes(trimOrNull(dto.getNotes()));
        return toDto(a);
    }

    @Transactional(readOnly = true)
    public AssetResponseDto getDto(Long id) {
        return toDto(get(id));
    }

    @Transactional(readOnly = true)
    public Page<AssetResponseDto> search(AssetSearchCriteria c, Pageable pageable) {
        Specification<Asset> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (c.getQuery() != null && !c.getQuery().isBlank()) {
                String like = "%" + c.getQuery().trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("assetTag")), like),
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("brand")), like),
                        cb.like(cb.lower(root.get("model")), like),
                        cb.like(cb.lower(root.get("serialNumber")), like)));
            }
            if (c.getCategoryId() != null) ps.add(cb.equal(root.get("category").get("id"), c.getCategoryId()));
            if (c.getLocationId() != null) ps.add(cb.equal(root.get("location").get("id"), c.getLocationId()));
            if (c.getStatus() != null) ps.add(cb.equal(root.get("status"), c.getStatus()));
            if (c.getSerialNumber() != null && !c.getSerialNumber().isBlank())
                ps.add(cb.equal(root.get("serialNumber"), c.getSerialNumber().trim()));
            if (c.getUnitName() != null && !c.getUnitName().isBlank())
                ps.add(cb.equal(root.get("location").get("unitName"), c.getUnitName().trim()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return assets.findAll(spec, pageable).map(this::toDto);
    }

    Asset get(Long id) {
        return assets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    public AssetResponseDto toDto(Asset a) {
        return AssetResponseDto.builder()
                .id(a.getId()).assetTag(a.getAssetTag()).name(a.getName())
                .brand(a.getBrand()).model(a.getModel())
                .serialNumber(a.getSerialNumber()).macAddress(a.getMacAddress())
                .status(a.getStatus())
                .categoryId(a.getCategory() != null ? a.getCategory().getId() : null)
                .categoryName(a.getCategory() != null ? a.getCategory().getName() : null)
                .locationId(a.getLocation() != null ? a.getLocation().getId() : null)
                .locationUnit(a.getLocation() != null ? a.getLocation().getUnitName() : null)
                .locationBuilding(a.getLocation() != null ? a.getLocation().getBuilding() : null)
                .locationRoom(a.getLocation() != null ? a.getLocation().getRoom() : null)
                .custodianId(a.getCurrentCustodian() != null ? a.getCurrentCustodian().getId() : null)
                .custodianName(a.getCurrentCustodian() != null ? a.getCurrentCustodian().getName() : null)
                .custodianStaffId(a.getCurrentCustodian() != null ? a.getCurrentCustodian().getWorkIdentifier() : null)
                .purchaseDate(a.getPurchaseDate()).warrantyExpiry(a.getWarrantyExpiry())
                .specifications(a.getSpecifications()).notes(a.getNotes())
                .qrCodeData(a.getQrCodeData())
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
                .build();
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
