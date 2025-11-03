package com.example.fuelticket.repository;

import com.example.fuelticket.entity.User;
import com.example.fuelticket.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTelephone(String telephone);
    List<User> findByStationsContains(Station station);
    List<User> findByStationsContainsAndEmailVerified(Station station, Boolean emailVerified);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
