package com.example.fuelticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCorpsRequest {
    @NotBlank(message = "Le nom du corps est obligatoire")
    private String nom;
    
    private String detail;
}

