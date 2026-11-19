package ng.edu.ui.items.dam.service;

import ng.edu.ui.items.dam.dto.CategoryDto;
import ng.edu.ui.items.dam.dto.LocationDto;
import ng.edu.ui.items.dam.entity.Category;
import ng.edu.ui.items.dam.entity.Location;
import ng.edu.ui.items.dam.exception.DuplicateResourceException;
import ng.edu.ui.items.dam.exception.ResourceNotFoundException;
import ng.edu.ui.items.dam.repository.AssetRepository;
import ng.edu.ui.items.dam.repository.CategoryRepository;
import ng.edu.ui.items.dam.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FR-10: reference lists editable by permitted staff.
 */
@Service
public class ReferenceDataService {

    private final CategoryRepository categories;
    private final LocationRepository locations;
    private final AssetRepository assets;

    public ReferenceDataService(CategoryRepository categories, LocationRepository locations, AssetRepository assets) {
        this.categories = categories;
        this.locations = locations;
        this.assets = assets;
    }

    // ---- categories ----
    @Transactional(readOnly = true)
    public List<CategoryDto> allCategories() {
        return categories.findAll().stream().map(this::toCat).toList();
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        if (categories.existsByName(dto.getName().trim())) {
            throw new DuplicateResourceException("Category already exists: " + dto.getName());
        }
        Category c = Category.builder()
                .name(dto.getName().trim()).code(dto.getCode()).description(dto.getDescription()).build();
        return toCat(categories.save(c));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category c = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categories.delete(c);
    }

    // ---- locations ----
    @Transactional(readOnly = true)
    public List<LocationDto> allLocations() {
        return locations.findAll().stream().map(this::toLoc).toList();
    }

    @Transactional
    public LocationDto createLocation(LocationDto dto) {
        Location l = Location.builder()
                .unitName(dto.getUnitName().trim()).building(dto.getBuilding().trim())
                .room(dto.getRoom()).campusZone(dto.getCampusZone()).build();
        return toLoc(locations.save(l));
    }

    @Transactional
    public void deleteLocation(Long id) {
        locations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
        locations.deleteById(id);
    }

    private CategoryDto toCat(Category c) {
        CategoryDto d = new CategoryDto();
        d.setId(c.getId()); d.setName(c.getName()); d.setCode(c.getCode()); d.setDescription(c.getDescription());
        return d;
    }

    private LocationDto toLoc(Location l) {
        LocationDto d = new LocationDto();
        d.setId(l.getId()); d.setUnitName(l.getUnitName()); d.setBuilding(l.getBuilding());
        d.setRoom(l.getRoom()); d.setCampusZone(l.getCampusZone());
        return d;
    }
}
