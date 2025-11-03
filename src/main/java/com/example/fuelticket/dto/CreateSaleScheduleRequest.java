package com.example.fuelticket.dto;

import com.example.fuelticket.entity.SaleSchedule;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateSaleScheduleRequest {
    @NotNull(message = "L'ID de la station est obligatoire")
    private Long stationId;
    
    @NotNull(message = "Le type de carburant est obligatoire")
    private SaleSchedule.FuelType fuelType;
    
    @NotNull(message = "La date de vente est obligatoire")
    @FutureOrPresent(message = "La date de vente doit être aujourd'hui ou dans le futur")
    private LocalDate saleDate;
    
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;
    
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;
    
    @NotNull(message = "La quantité disponible est obligatoire")
    @Positive(message = "La quantité disponible doit être positive")
    private Double availableQuantity;
    
    @NotNull(message = "La quantité maximale par ticket est obligatoire")
    @Positive(message = "La quantité maximale par ticket doit être positive")
    private Double maxQuantityPerTicket;
    
    @NotNull(message = "Le nombre maximum de tickets par jour est obligatoire")
    @Min(value = 1, message = "Le nombre maximum de tickets doit être au moins 1")
    private Integer maxTicketsPerDay;
    
    private Boolean isActive = true;
}
