package ng.edu.ui.items.dam.controller;

import ng.edu.ui.items.dam.dto.CategoryDto;
import ng.edu.ui.items.dam.dto.LocationDto;
import ng.edu.ui.items.dam.service.ReferenceDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * FR-10: reference lists editable by permitted staff.
 */
@RestController
@RequestMapping("/reference")
public class ReferenceController {

    private final ReferenceDataService ref;

    public ReferenceController(ReferenceDataService ref) {
        this.ref = ref;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> categories() {
        return ResponseEntity.ok(ref.allCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto dto) {
        CategoryDto saved = ref.createCategory(dto);
        return ResponseEntity.created(URI.create("/api/v1/reference/categories/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        ref.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationDto>> locations() {
        return ResponseEntity.ok(ref.allLocations());
    }

    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto) {
        LocationDto saved = ref.createLocation(dto);
        return ResponseEntity.created(URI.create("/api/v1/reference/locations/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ASSET_OFFICER')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        ref.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
