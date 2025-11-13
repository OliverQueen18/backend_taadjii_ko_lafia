package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.DynamicUpdate
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(unique = true)
    private String telephone; // Numéro de téléphone (unique)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "VARCHAR(20)") // Longueur suffisante pour GESTIONNAIRE (12 caractères)
    private Role role;
    
    // Champs pour la validation par email
    private Boolean emailVerified = false; // Email vérifié ou non
    private String verificationCode; // Code de vérification email
    private LocalDateTime verificationCodeExpiry; // Expiration du code email
    private LocalDateTime verificationCodeSentAt; // Date d'envoi du code email
    
    // Champs pour la validation par téléphone
    private Boolean telephoneVerified = false; // Téléphone vérifié ou non
    private String telephoneVerificationCode; // Code de vérification téléphone
    private LocalDateTime telephoneVerificationCodeExpiry; // Expiration du code téléphone
    private LocalDateTime telephoneVerificationCodeSentAt; // Date d'envoi du code téléphone
    
    // Relations: un gérant peut gérer plusieurs stations
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.User.stations")
    private java.util.List<Station> stations;
    
    // Champs de sécurité pour la protection contre les tentatives de connexion
    @Builder.Default
    private Integer failedLoginAttempts = 0; // Nombre de tentatives de connexion échouées
    @Builder.Default
    private LocalDateTime accountLockedUntil = null; // Date/heure jusqu'à laquelle le compte est bloqué
    @Builder.Default
    private LocalDateTime lastFailedLoginAttempt = null; // Date/heure de la dernière tentative échouée
    
    // Champs d'audit
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Role { CITOYEN, STATION, ADMIN, GESTIONNAIRE }
    
    /**
     * Vérifie si le compte est actuellement bloqué
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(accountLockedUntil);
    }
    
    /**
     * Retourne le nombre de minutes restantes avant le déblocage
     */
    public long getMinutesUntilUnlock() {
        if (accountLockedUntil == null) {
            return 0;
        }
        if (LocalDateTime.now().isAfter(accountLockedUntil)) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), accountLockedUntil).toMinutes();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
