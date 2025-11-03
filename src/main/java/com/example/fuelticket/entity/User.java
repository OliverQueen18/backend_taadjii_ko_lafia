package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private java.util.List<Station> stations;
    
    // Champs d'audit
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Role { CITOYEN, STATION, ADMIN }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
