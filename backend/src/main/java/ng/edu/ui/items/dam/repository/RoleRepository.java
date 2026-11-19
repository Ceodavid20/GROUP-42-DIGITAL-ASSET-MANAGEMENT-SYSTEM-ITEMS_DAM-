package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.Role;
import ng.edu.ui.items.dam.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
