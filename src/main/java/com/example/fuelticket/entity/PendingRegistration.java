package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité pour stocker temporairement les inscriptions en attente de vérification
 * L'utilisateur ne sera créé dans la table User qu'après vérification du code
 */
@Entity
@Table(name = "pending_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(unique = true)
    private String telephone; // Peut être null pour les non-citoyens
    
    @Column(nullable = false, length = 6)
    private String verificationCode; // Code de vérification email
    
    @Column(nullable = false)
    private LocalDateTime verificationCodeExpiry; // Expiration du code
    
    @Column
    private String telephoneVerificationCode; // Code de vérification SMS (optionnel)
    
    @Column
    private LocalDateTime telephoneVerificationCodeExpiry;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean telephoneVerified = false; // Indique si le téléphone a été vérifié
    
    // Stocker les données utilisateur en JSON pour faciliter la création après vérification
    @Column(columnDefinition = "TEXT", nullable = false)
    private String userDataJson; // JSON contenant toutes les données UserDto + StationDto
    
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime verifiedAt; // Date de vérification (si vérifié)
    
    /**
     * Vérifie si le code de vérification email est expiré
     */
    public boolean isEmailCodeExpired() {
        return verificationCodeExpiry == null || LocalDateTime.now().isAfter(verificationCodeExpiry);
    }
    
    /**
     * Vérifie si le code de vérification téléphone est expiré
     */
    public boolean isTelephoneCodeExpired() {
        return telephoneVerificationCodeExpiry == null || 
               LocalDateTime.now().isAfter(telephoneVerificationCodeExpiry);
    }
}

