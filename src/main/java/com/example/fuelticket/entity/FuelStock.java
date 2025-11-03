package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;
    
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;
    
    private Double stockDisponible; // en litres
    private Double capaciteMaximale; // en litres
    private Double prixParLitre; // en FCFA
    private Boolean isDisponible; // si le type de carburant est disponible
    
    public enum FuelType {
        ESSENCE("Essence"),
        DIESEL("Diesel"),
        GPL("GPL"),
        KEROSENE("Kérosène");
        
        private final String displayName;
        
        FuelType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}

