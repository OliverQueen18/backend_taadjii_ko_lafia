package com.example.fuelticket.dto;

import com.example.fuelticket.entity.FuelStock;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelStockDto {
    private Long id;
    private Long stationId;
    private FuelStock.FuelType fuelType;
    private Double stockDisponible;
    private Double capaciteMaximale;
    private Double prixParLitre;
    private Boolean isDisponible;
    private String displayName;
    
    public static FuelStockDto fromEntity(FuelStock fuelStock) {
        return FuelStockDto.builder()
                .id(fuelStock.getId())
                .stationId(fuelStock.getStation().getId())
                .fuelType(fuelStock.getFuelType())
                .stockDisponible(fuelStock.getStockDisponible())
                .capaciteMaximale(fuelStock.getCapaciteMaximale())
                .prixParLitre(fuelStock.getPrixParLitre())
                .isDisponible(fuelStock.getIsDisponible())
                .displayName(fuelStock.getFuelType().getDisplayName())
                .build();
    }
}

