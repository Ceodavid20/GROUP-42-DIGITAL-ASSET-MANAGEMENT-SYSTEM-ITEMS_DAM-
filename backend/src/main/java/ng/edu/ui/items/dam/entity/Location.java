package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Location Entity (Table 3.3 Data Dictionary)
 * id, unit, building, room
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String unitName; // e.g. "Network and Hardware Unit", "MIS", "Software Development"

    @Column(nullable = false, length = 120)
    private String building; // e.g. "ITeMS Directorate Complex", "Trenchard Hall", "KDL"

    @Column(length = 60)
    private String room; // e.g. "Hardware Lab 102", "Server Room B", "Media Studio 1"

    @Column(length = 60)
    private String campusZone; // e.g. "Main Campus Agbowo", "College of Medicine UCH"
}
