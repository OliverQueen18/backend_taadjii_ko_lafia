package com.example.fuelticket.dto;

import com.example.fuelticket.entity.FuelStock;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateApprovisionnementRequest {
    @NotNull(message = "L'ID de la société est obligatoire")
    private Long societeId;
    
    @NotNull(message = "L'ID de la station est obligatoire")
    private Long stationId;
    
    @NotNull(message = "Le type de carburant est obligatoire")
    private FuelStock.FuelType fuelType;
    
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Double quantite;
    
    @NotNull(message = "La date d'approvisionnement est obligatoire")
    private LocalDateTime dateApprovisionnement;
    
    private String numeroCiterne;
    private String numeroBonLivraison;
    private String commentaire;
}

