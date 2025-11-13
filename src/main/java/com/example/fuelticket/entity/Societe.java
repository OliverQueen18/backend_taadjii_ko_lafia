package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "societe")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.Societe")
public class Societe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom; // Nom de la compagnie pétrolière
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    
    // Logo ou image de la société
    private String logoUrl;
    
    // Champs d'audit
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Relation: une société peut avoir plusieurs approvisionnements
    @OneToMany(mappedBy = "societe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Approvisionnement> approvisionnements;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

