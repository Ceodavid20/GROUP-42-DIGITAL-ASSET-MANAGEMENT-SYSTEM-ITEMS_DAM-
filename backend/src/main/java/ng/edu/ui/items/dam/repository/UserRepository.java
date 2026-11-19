package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByWorkIdentifier(String workIdentifier);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByWorkIdentifier(String workIdentifier);
}
