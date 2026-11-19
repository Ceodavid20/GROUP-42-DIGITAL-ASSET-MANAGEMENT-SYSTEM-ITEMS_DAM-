package ng.edu.ui.items.dam.dto;

import lombok.Data;
import ng.edu.ui.items.dam.enums.AssetStatus;

@Data
public class AssetSearchCriteria {
    private String query; // matches tag, name, brand, model, serial
    private Long categoryId;
    private Long locationId;
    private String unitName;
    private AssetStatus status;
    private String serialNumber;
}
