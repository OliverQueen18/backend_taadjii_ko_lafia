package com.example.fuelticket.repository;

import com.example.fuelticket.entity.FuelStock;
import com.example.fuelticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FuelStockRepository extends JpaRepository<FuelStock, Long> {
    List<FuelStock> findByStation(Station station);
    List<FuelStock> findByStationAndFuelType(Station station, FuelStock.FuelType fuelType);
    List<FuelStock> findByStationAndIsDisponibleTrue(Station station);
    
    @Query("SELECT fs FROM FuelStock fs WHERE fs.station = :station AND fs.fuelType = :fuelType AND fs.isDisponible = true AND fs.stockDisponible > 0")
    List<FuelStock> findAvailableFuelStocksByStationAndType(@Param("station") Station station, @Param("fuelType") FuelStock.FuelType fuelType);
    
    @Query("SELECT fs FROM FuelStock fs WHERE fs.station = :station AND fs.isDisponible = true AND fs.stockDisponible > 0")
    List<FuelStock> findAvailableFuelStocksByStation(@Param("station") Station station);
    
    boolean existsByStationAndFuelType(Station station, FuelStock.FuelType fuelType);
}

