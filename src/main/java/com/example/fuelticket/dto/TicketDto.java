package com.example.fuelticket.dto;

import com.example.fuelticket.entity.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TicketDto {
    private Long id;
    private String numeroTicket;
    private String numeroOrdre;
    private LocalDate dateApprovisionnement;

    @NotBlank(message = "Type carburant is required")
    private String typeCarburant;

    @NotNull(message = "Quantite is required")
    @Positive(message = "Quantite must be positive")
    private Double quantite;

    private Ticket.Statut statut;
    private Long citoyenId;
    private Long stationId;
    
    // Informations du citoyen (remplies lors de la création)
    private String emailCitoyen;
    private String nomCitoyen;
    private String prenomCitoyen;
    private String telephoneCitoyen;
    
    // Dates
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    
    // QR Code
    private String qrCodeData; // Données du QR code
}
