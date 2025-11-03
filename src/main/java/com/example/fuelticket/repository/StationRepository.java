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
}
