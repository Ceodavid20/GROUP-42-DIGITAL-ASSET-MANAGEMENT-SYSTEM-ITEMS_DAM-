package ng.edu.ui.items.dam.entity;

import jakarta.persistence.*;
import lombok.*;
import ng.edu.ui.items.dam.enums.ERole;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, unique = true, nullable = false)
    private ERole name;

    @Column(length = 255)
    private String description;
}
