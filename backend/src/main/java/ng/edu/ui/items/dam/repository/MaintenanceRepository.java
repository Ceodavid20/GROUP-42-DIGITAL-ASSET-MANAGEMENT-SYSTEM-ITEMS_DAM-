package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.Maintenance;
import ng.edu.ui.items.dam.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    List<Maintenance> findByAssetIdOrderByOpenedOnDesc(Long assetId);
    Page<Maintenance> findByStatus(MaintenanceStatus status, Pageable pageable);
    long countByStatus(MaintenanceStatus status);
}
