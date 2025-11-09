package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "region")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code; // Code unique de la région (ex: "BKO", "KAY", etc.)
    
    @Column(nullable = false)
    private String nom;
    
    // Géolocalisation (coordonnées du centre de la région)
    private Double latitude;
    private Double longitude;
    
    // Relation: une région peut avoir plusieurs stations
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Station> stations;
}

