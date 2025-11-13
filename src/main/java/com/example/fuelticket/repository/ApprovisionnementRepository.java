package com.example.fuelticket.repository;

import com.example.fuelticket.entity.Approvisionnement;
import com.example.fuelticket.entity.Societe;
import com.example.fuelticket.entity.Region;
import com.example.fuelticket.entity.FuelStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApprovisionnementRepository extends JpaRepository<Approvisionnement, Long> {
    List<Approvisionnement> findBySociete(Societe societe);
    List<Approvisionnement> findByRegion(Region region);
    List<Approvisionnement> findByRegionAndFuelType(Region region, FuelStock.FuelType fuelType);
    List<Approvisionnement> findByDateApprovisionnementBetween(LocalDateTime start, LocalDateTime end);
    List<Approvisionnement> findBySocieteAndDateApprovisionnementBetween(Societe societe, LocalDateTime start, LocalDateTime end);
}

