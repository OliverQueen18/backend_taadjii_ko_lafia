package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.Station")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String localisation;
    private String adresseComplete;
    private Double capaciteJournaliere; // litres
    
    // Coordonnées géographiques
    private Double latitude;
    private Double longitude;
    
    // Informations de contact
    private String telephone;
    private String email;
    private String siteWeb;
    
    // Horaires d'ouverture
    private String horairesOuverture; // ex: "06:00-22:00"
    private Boolean isOuverte; // si la station est actuellement ouverte
    
    // Relations
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.Station.fuelStocks")
    private List<FuelStock> fuelStocks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.Station.repartitions")
    private List<Repartition> repartitions;
    
    // Méthodes utilitaires
    public Double getStockTotalDisponible() {
        return fuelStocks != null ? 
            fuelStocks.stream()
                .filter(FuelStock::getIsDisponible)
                .mapToDouble(FuelStock::getStockDisponible)
                .sum() : 0.0;
    }
    
    public List<FuelStock.FuelType> getTypesCarburantDisponibles() {
        return fuelStocks != null ?
            fuelStocks.stream()
                .filter(FuelStock::getIsDisponible)
                .filter(stock -> stock.getStockDisponible() > 0)
                .map(FuelStock::getFuelType)
                .toList() : List.of();
    }
}
