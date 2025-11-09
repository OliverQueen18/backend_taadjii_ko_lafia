package com.example.fuelticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRepartitionRequest {
    @NotNull(message = "L'ID du corps est obligatoire")
    private Long corpsId;
    
    @NotNull(message = "L'ID de la station est obligatoire")
    private Long stationId;
    
    private String commentaire;
}

