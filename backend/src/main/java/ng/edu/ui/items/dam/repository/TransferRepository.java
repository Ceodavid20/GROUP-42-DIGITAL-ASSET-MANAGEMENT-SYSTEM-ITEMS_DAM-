package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.Transfer;
import ng.edu.ui.items.dam.enums.TransferDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByAssetIdOrderByRequestedAtDesc(Long assetId);
    Page<Transfer> findByDecision(TransferDecision decision, Pageable pageable);
    long countByDecision(TransferDecision decision);
}
