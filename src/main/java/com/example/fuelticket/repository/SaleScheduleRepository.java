package com.example.fuelticket.repository;

import com.example.fuelticket.entity.SaleSchedule;
import com.example.fuelticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleScheduleRepository extends JpaRepository<SaleSchedule, Long> {
    List<SaleSchedule> findByStation(Station station);
    List<SaleSchedule> findByStationAndIsActiveTrue(Station station);
    List<SaleSchedule> findByStationAndSaleDate(Station station, LocalDate saleDate);
    List<SaleSchedule> findByStationAndSaleDateAndIsActiveTrue(Station station, LocalDate saleDate);
    
    @Query("SELECT s FROM SaleSchedule s WHERE s.station = :station AND s.saleDate >= :startDate AND s.saleDate <= :endDate AND s.isActive = true ORDER BY s.saleDate, s.startTime")
    List<SaleSchedule> findActiveSchedulesByStationAndDateRange(@Param("station") Station station, 
                                                               @Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM SaleSchedule s WHERE s.station = :station AND s.saleDate = :date AND s.fuelType = :fuelType AND s.isActive = true")
    List<SaleSchedule> findActiveSchedulesByStationDateAndFuelType(@Param("station") Station station, 
                                                                   @Param("date") LocalDate date, 
                                                                   @Param("fuelType") SaleSchedule.FuelType fuelType);
    
    @Query("SELECT DISTINCT s FROM SaleSchedule s LEFT JOIN FETCH s.tickets WHERE s.station = :station AND s.saleDate = :date AND s.fuelType = :fuelType AND s.isActive = true")
    List<SaleSchedule> findActiveSchedulesByStationDateAndFuelTypeWithTickets(@Param("station") Station station, 
                                                                              @Param("date") LocalDate date, 
                                                                              @Param("fuelType") SaleSchedule.FuelType fuelType);
    
    @Query("SELECT s FROM SaleSchedule s WHERE s.saleDate < :date AND s.isActive = true")
    List<SaleSchedule> findExpiredSchedules(@Param("date") LocalDate date);
    
    boolean existsByStationAndSaleDateAndFuelTypeAndIsActiveTrue(Station station, LocalDate saleDate, SaleSchedule.FuelType fuelType);
}
