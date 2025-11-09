package com.example.fuelticket.repository;

import com.example.fuelticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByManagerId(Long managerId);
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.manager WHERE s.id = :id")
    Optional<Station> findByIdWithManager(@Param("id") Long id);
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.region LEFT JOIN FETCH s.fuelStocks WHERE s.manager.id = :managerId")
    List<Station> findByManagerIdWithRegionAndStocks(@Param("managerId") Long managerId);
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.region LEFT JOIN FETCH s.fuelStocks")
    List<Station> findAllWithRegionAndStocks();
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.region LEFT JOIN FETCH s.fuelStocks WHERE s.id = :id")
    Optional<Station> findByIdWithRegionAndStocks(@Param("id") Long id);
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.region")
    List<Station> findAllWithRegion();
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.region WHERE s.manager.id = :managerId")
    List<Station> findByManagerIdWithRegion(@Param("managerId") Long managerId);
    
    @Query("SELECT COUNT(DISTINCT s) FROM Station s JOIN s.fuelStocks fs WHERE fs.isDisponible = true AND fs.stockDisponible > 0")
    long countStationsWithAvailableStock();
}
