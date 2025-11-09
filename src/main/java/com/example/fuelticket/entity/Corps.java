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
@Table(name = "corps")
public class Corps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(columnDefinition = "TEXT")
    private String detail;
    
    // Relation n-n avec Station via Répartition
    @OneToMany(mappedBy = "corps", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Repartition> repartitions;
}

