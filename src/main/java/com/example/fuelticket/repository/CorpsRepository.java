package com.example.fuelticket.repository;

import com.example.fuelticket.entity.Corps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorpsRepository extends JpaRepository<Corps, Long> {
    Optional<Corps> findByNom(String nom);
}

