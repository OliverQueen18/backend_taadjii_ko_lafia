package com.example.fuelticket.service;

import com.example.fuelticket.dto.FuelMovementDto;
import com.example.fuelticket.entity.FuelMovement;
import com.example.fuelticket.entity.FuelStock;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.repository.FuelMovementRepository;
import com.example.fuelticket.repository.FuelStockRepository;
import com.example.fuelticket.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelMovementService {
    
    private final FuelMovementRepository fuelMovementRepository;
    private final StationRepository stationRepository;
    private final FuelStockRepository fuelStockRepository;
    private final AuthService authService;

    public List<FuelMovementDto> getMovementsByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return fuelMovementRepository.findByStationOrderByDateDesc(station).stream()
                .map(FuelMovementDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FuelMovementDto> getMovementsByStationAndDateRange(Long stationId, LocalDate startDate, LocalDate endDate) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        
        return fuelMovementRepository.findByStationAndDateBetween(station, startDateTime, endDateTime).stream()
                .map(FuelMovementDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FuelMovementDto> getMovementsByMyStations() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        
        // Récupérer toutes les stations de l'utilisateur
        List<Station> stations = stationRepository.findByManagerId(currentUser.getId());
        
        if (stations.isEmpty()) {
            return List.of();
        }
        
        LocalDateTime startOfWeek = LocalDate.now().atStartOfDay().minusDays(7);
        LocalDateTime endDateTime = LocalDateTime.now();
        
        return fuelMovementRepository.findByStationsAndDateBetween(stations, startOfWeek, endDateTime).stream()
                .map(FuelMovementDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FuelMovementDto> getMovementsByStock(Long stockId) {
        FuelStock stock = fuelStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock de carburant non trouvé"));
        
        return fuelMovementRepository.findByStockOrderByDateDesc(stock).stream()
                .map(FuelMovementDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FuelMovementDto createMovement(Long stockId, FuelMovement.MovementType type, Double quantity, String description) {
        FuelStock stock = fuelStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock de carburant non trouvé"));
        
        User currentUser = authService.getCurrentUser();
        
        FuelMovement movement = FuelMovement.builder()
                .stock(stock)
                .station(stock.getStation())
                .type(type)
                .quantity(quantity)
                .description(description)
                .prixUnitaire(stock.getPrixParLitre())
                .createdBy(currentUser)
                .build();
        
        FuelMovement savedMovement = fuelMovementRepository.save(movement);
        return FuelMovementDto.fromEntity(savedMovement);
    }
}

