package com.example.fuelticket.repository;

import com.example.fuelticket.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    
    /**
     * Trouve une inscription en attente par email
     */
    Optional<PendingRegistration> findByEmail(String email);
    
    /**
     * Trouve une inscription en attente par téléphone
     */
    Optional<PendingRegistration> findByTelephone(String telephone);
    
    /**
     * Vérifie si un email existe déjà dans les inscriptions en attente
     */
    boolean existsByEmail(String email);
    
    /**
     * Vérifie si un téléphone existe déjà dans les inscriptions en attente
     */
    boolean existsByTelephone(String telephone);
    
    /**
     * Supprime toutes les inscriptions expirées (codes expirés depuis plus de 24h)
     */
    @Modifying
    @Query("DELETE FROM PendingRegistration p WHERE p.verificationCodeExpiry < :expiryDate")
    int deleteExpiredRegistrations(@Param("expiryDate") LocalDateTime expiryDate);
}

