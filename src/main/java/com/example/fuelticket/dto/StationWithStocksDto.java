package com.example.fuelticket.dto;

import com.example.fuelticket.entity.FuelStock;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationWithStocksDto {
    private Long id;
    private String nom;
    private String localisation;
    private String adresseComplete;
    private Double capaciteJournaliere;
    private Double latitude;
    private Double longitude;
    private String telephone;
    private String email;
    private String siteWeb;
    private String horairesOuverture;
    private Boolean isOuverte;
    private Double stockTotalDisponible;
    private List<FuelStockDto> fuelStocks;
    private List<String> typesCarburantDisponibles;
    private Long managerId; // ID du gérant de la station
    
    public static StationWithStocksDto fromStationWithStocks(com.example.fuelticket.entity.Station station) {
        List<FuelStockDto> fuelStockDtos = station.getFuelStocks() != null ?
            station.getFuelStocks().stream()
                .map(FuelStockDto::fromEntity)
                .toList() : List.of();
                
        List<String> typesDisponibles = station.getTypesCarburantDisponibles().stream()
            .map(FuelStock.FuelType::getDisplayName)
            .toList();
        
        return StationWithStocksDto.builder()
                .id(station.getId())
                .nom(station.getNom())
                .localisation(station.getLocalisation())
                .adresseComplete(station.getAdresseComplete())
                .capaciteJournaliere(station.getCapaciteJournaliere())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .telephone(station.getTelephone())
                .email(station.getEmail())
                .siteWeb(station.getSiteWeb())
                .horairesOuverture(station.getHorairesOuverture())
                .isOuverte(station.getIsOuverte())
                .stockTotalDisponible(station.getStockTotalDisponible())
                .fuelStocks(fuelStockDtos)
                .typesCarburantDisponibles(typesDisponibles)
                .managerId(station.getManager() != null ? station.getManager().getId() : null)
                .build();
    }
}

