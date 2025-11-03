package com.example.fuelticket.dto;

import com.example.fuelticket.entity.FuelMovement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelMovementDto {
    private Long id;
    private Long stockId;
    private Long stationId;
    private String stationName;
    private String fuelType;
    private String type; // ENTREE ou SORTIE
    private Double quantity;
    private String description;
    private Double prixUnitaire;
    private Double montantTotal;
    private String date;
    private Long createdById;
    private String createdByEmail;
    
    public static FuelMovementDto fromEntity(FuelMovement movement) {
        if (movement == null) {
            return null;
        }
        
        FuelMovementDto.FuelMovementDtoBuilder builder = FuelMovementDto.builder()
                .id(movement.getId())
                .stockId(movement.getStock() != null ? movement.getStock().getId() : null)
                .stationId(movement.getStation() != null ? movement.getStation().getId() : null)
                .stationName(movement.getStation() != null ? movement.getStation().getNom() : null)
                .fuelType(movement.getStock() != null && movement.getStock().getFuelType() != null 
                    ? movement.getStock().getFuelType().getDisplayName() : null)
                .type(movement.getType() != null ? movement.getType().name() : null)
                .quantity(movement.getQuantity())
                .description(movement.getDescription())
                .prixUnitaire(movement.getPrixUnitaire())
                .montantTotal(movement.getMontantTotal())
                .date(movement.getDate() != null ? movement.getDate().toString() : null)
                .createdById(movement.getCreatedBy() != null ? movement.getCreatedBy().getId() : null)
                .createdByEmail(movement.getCreatedBy() != null ? movement.getCreatedBy().getEmail() : null);
        
        return builder.build();
    }
}

