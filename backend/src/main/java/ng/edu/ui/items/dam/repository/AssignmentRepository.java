package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByAssetIdAndActiveTrue(Long assetId);
    List<Assignment> findByAssetIdOrderByAssignedOnDesc(Long assetId);
    Page<Assignment> findByCustodianId(Long custodianId, Pageable pageable);
}
