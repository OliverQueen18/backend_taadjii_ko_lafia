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
@Table(name = "approvisionnement")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.Approvisionnement")
public class Approvisionnement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "societe_id", nullable = false)
    private Societe societe;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelStock.FuelType fuelType; // Type de carburant
    
    @Column(nullable = false)
    private Double quantite; // Quantité en litres
    
    @Column(nullable = false)
    private LocalDateTime dateApprovisionnement; // Date et heure de l'approvisionnement
    
    private String numeroCiterne; // Numéro de la citerne
    private String numeroBonLivraison; // Numéro du bon de livraison
    
    @Column(columnDefinition = "TEXT")
    private String commentaire; // Commentaires additionnels
    
    // Champs d'audit
    private LocalDateTime createdAt = LocalDateTime.now();
}

