package com.example.fuelticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StationDto {
    private Long id;

    @NotBlank(message = "Nom is required")
    @Size(min = 2, max = 100, message = "Nom must be between 2 and 100 characters")
    private String nom;

    @NotBlank(message = "Localisation is required")
    @Size(min = 5, max = 200, message = "Localisation must be between 5 and 200 characters")
    private String localisation;

    @NotNull(message = "Capacite journaliere is required")
    @Positive(message = "Capacite journaliere must be positive")
    private Double capaciteJournaliere;

    // Champs optionnels supplémentaires
    private String adresseComplete;
    private Double latitude;
    private Double longitude;
    private String telephone;
    private String email;
    private String siteWeb;
    private String horairesOuverture;
    private Boolean isOuverte;
    private Long managerId; // ID du gérant de la station
}
