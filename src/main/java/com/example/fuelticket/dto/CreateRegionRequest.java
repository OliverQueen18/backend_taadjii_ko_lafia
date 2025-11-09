package com.example.fuelticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRegionRequest {
    @NotBlank(message = "Le code de la région est obligatoire")
    private String code;
    
    @NotBlank(message = "Le nom de la région est obligatoire")
    private String nom;
    
    private Double latitude;
    private Double longitude;
}

