package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUnitName(String unitName);
}
