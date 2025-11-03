package com.example.fuelticket.repository;

import com.example.fuelticket.entity.FuelMovement;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.FuelStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FuelMovementRepository extends JpaRepository<FuelMovement, Long> {
    List<FuelMovement> findByStation(Station station);
    
    List<FuelMovement> findByStock(FuelStock stock);
    
    List<FuelMovement> findByStationOrderByDateDesc(Station station);
    
    List<FuelMovement> findByStockOrderByDateDesc(FuelStock stock);
    
    @Query("SELECT fm FROM FuelMovement fm WHERE fm.station = :station AND fm.date >= :startDate AND fm.date <= :endDate ORDER BY fm.date DESC")
    List<FuelMovement> findByStationAndDateBetween(@Param("station") Station station, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT fm FROM FuelMovement fm WHERE fm.station IN :stations AND fm.date >= :startDate AND fm.date <= :endDate ORDER BY fm.date DESC")
    List<FuelMovement> findByStationsAndDateBetween(@Param("stations") List<Station> stations, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
}

