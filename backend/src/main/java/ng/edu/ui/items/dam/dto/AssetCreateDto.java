package ng.edu.ui.items.dam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssetCreateDto {

    @NotBlank(message = "Institutional asset tag is required (e.g. UI-ITEMS-DEMO-001)")
    private String assetTag;

    @NotBlank(message = "Asset name or description is required")
    private String name;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Location is required")
    private Long locationId;

    private String brand;
    private String model;
    private String serialNumber;
    private String macAddress;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiry;
    private String specifications;
    private String notes;
}
