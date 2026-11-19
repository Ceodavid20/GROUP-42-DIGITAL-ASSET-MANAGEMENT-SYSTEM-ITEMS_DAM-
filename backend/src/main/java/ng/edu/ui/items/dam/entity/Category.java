package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Category Entity (Table 3.3 Data Dictionary)
 * id, name, description, code
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // e.g. "Computing Devices", "Network Hardware", "Power Equipment"

    @Column(length = 30)
    private String code; // e.g. "COMP", "NET", "PWR", "DISP", "SRV"

    @Column(length = 255)
    private String description;
}
