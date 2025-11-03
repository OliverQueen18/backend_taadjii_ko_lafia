package com.example.fuelticket.dto;

import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashTicketRequest {
    @NotBlank(message = "L'email du citoyen est obligatoire")
    @Email(message = "Format d'email invalide")
    private String emailCitoyen;
    
    @NotBlank(message = "Le nom du citoyen est obligatoire")
    private String nomCitoyen;
    
    @NotBlank(message = "Le prénom du citoyen est obligatoire")
    private String prenomCitoyen;
    
    private String telephoneCitoyen;
    
    @NotNull(message = "La date d'approvisionnement est obligatoire")
    private LocalDate dateApprovisionnement;
    
    @NotBlank(message = "Le type de carburant est obligatoire")
    private String typeCarburant;
    
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private Double quantite;
    
    @NotNull(message = "Le montant payé est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Double montantPaye;
    
    @NotNull(message = "L'ID de la station est obligatoire")
    private Long stationId;
}
