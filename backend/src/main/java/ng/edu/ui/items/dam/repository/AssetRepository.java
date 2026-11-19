package ng.edu.ui.items.dam.repository;

import ng.edu.ui.items.dam.entity.Asset;
import ng.edu.ui.items.dam.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByAssetTag(String assetTag);

    boolean existsByAssetTag(String assetTag);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    @Query("SELECT a.status, COUNT(a) FROM Asset a GROUP BY a.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT a.category.name, COUNT(a) FROM Asset a GROUP BY a.category.name")
    List<Object[]> countGroupedByCategory();

    @Query("SELECT a.location.unitName, COUNT(a) FROM Asset a GROUP BY a.location.unitName")
    List<Object[]> countGroupedByUnit();

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = 'UNDER_MAINTENANCE'")
    long countUnderMaintenance();

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = 'REGISTERED'")
    long countUnverifiedRegistered();
}
