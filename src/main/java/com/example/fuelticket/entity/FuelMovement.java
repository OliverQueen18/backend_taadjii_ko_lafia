package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fuel_movement")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.FuelMovement")
public class FuelMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private FuelStock stock;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;
    
    @Column(nullable = false)
    private Double quantity; // en litres
    
    private String description;
    
    private Double prixUnitaire; // Prix par litre au moment du mouvement
    
    private Double montantTotal; // quantity * prixUnitaire
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdBy; // Utilisateur qui a effectué le mouvement
    
    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        // Calculer le montant total si le prix unitaire est disponible
        if (prixUnitaire != null && quantity != null) {
            montantTotal = quantity * prixUnitaire;
        }
    }
    
    public enum MovementType {
        ENTREE,
        SORTIE
    }
}

