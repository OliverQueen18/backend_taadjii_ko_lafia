package com.example.fuelticket.repository;

import com.example.fuelticket.entity.Repartition;
import com.example.fuelticket.entity.Corps;
import com.example.fuelticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepartitionRepository extends JpaRepository<Repartition, Long> {
    List<Repartition> findByCorps(Corps corps);
    List<Repartition> findByStation(Station station);
    Optional<Repartition> findByCorpsAndStation(Corps corps, Station station);
    boolean existsByCorpsAndStation(Corps corps, Station station);
}

