package com.example.fuelticket.dto;

import com.example.fuelticket.entity.FuelStock;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovisionnementDto {
    private Long id;
    private Long societeId;
    private String societeNom;
    private Long stationId;
    private String stationNom;
    private FuelStock.FuelType fuelType;
    private String fuelTypeDisplayName;
    private Double quantite;
    private LocalDateTime dateApprovisionnement;
    private String numeroCiterne;
    private String numeroBonLivraison;
    private String commentaire;
    private LocalDateTime createdAt;
}

