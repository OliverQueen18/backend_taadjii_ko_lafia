package com.example.fuelticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSocieteRequest {
    @NotBlank(message = "Le nom de la société est obligatoire")
    private String nom;
    
    private String description;
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    private String logoUrl;
}

