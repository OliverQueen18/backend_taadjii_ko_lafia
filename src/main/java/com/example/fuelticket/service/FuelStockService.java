package com.example.fuelticket.service;

import com.example.fuelticket.dto.FuelStockDto;
import com.example.fuelticket.entity.FuelMovement;
import com.example.fuelticket.entity.FuelStock;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.exception.BadRequestException;
import com.example.fuelticket.repository.FuelMovementRepository;
import com.example.fuelticket.repository.FuelStockRepository;
import com.example.fuelticket.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuelStockService {
    private final FuelStockRepository fuelStockRepository;
    private final StationRepository stationRepository;
    private final FuelMovementRepository fuelMovementRepository;
    private final AuthService authService;

    public List<FuelStockDto> getFuelStocksByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return fuelStockRepository.findByStation(station).stream()
                .map(FuelStockDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FuelStockDto> getAvailableFuelStocksByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return fuelStockRepository.findAvailableFuelStocksByStation(station).stream()
                .map(FuelStockDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FuelStockDto updateFuelStock(Long stockId, Double newStock, Double newPrice) {
        FuelStock fuelStock = fuelStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock de carburant non trouvé"));
        
        fuelStock.setStockDisponible(newStock);
        if (newPrice != null) {
            fuelStock.setPrixParLitre(newPrice);
        }
        
        FuelStock savedStock = fuelStockRepository.save(fuelStock);
        return FuelStockDto.fromEntity(savedStock);
    }

    @Transactional
    public FuelStockDto createFuelStock(Long stationId, FuelStock.FuelType fuelType, 
                                       Double stockInitial, Double capaciteMax, Double prix) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        // Vérifier si ce type de carburant existe déjà pour cette station
        if (fuelStockRepository.existsByStationAndFuelType(station, fuelType)) {
            throw new RuntimeException("Ce type de carburant existe déjà pour cette station");
        }
        
        FuelStock fuelStock = FuelStock.builder()
                .station(station)
                .fuelType(fuelType)
                .stockDisponible(stockInitial)
                .capaciteMaximale(capaciteMax)
                .prixParLitre(prix)
                .isDisponible(true)
                .build();
        
        FuelStock savedStock = fuelStockRepository.save(fuelStock);
        return FuelStockDto.fromEntity(savedStock);
    }

    @Transactional
    public void deleteFuelStock(Long stockId) {
        fuelStockRepository.deleteById(stockId);
    }

    @Transactional
    public FuelStockDto toggleFuelAvailability(Long stockId) {
        FuelStock fuelStock = fuelStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock de carburant non trouvé"));
        
        fuelStock.setIsDisponible(!fuelStock.getIsDisponible());
        FuelStock savedStock = fuelStockRepository.save(fuelStock);
        return FuelStockDto.fromEntity(savedStock);
    }

    @Transactional
    public FuelStockDto addFuel(Long stockId, Double quantity) {
        FuelStock fuelStock = fuelStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock de carburant non trouvé"));
        
        // S'assurer que la station est chargée
        Station station = fuelStock.getStation();
        if (station == null) {
            log.error("Station is null for fuel stock ID: {}", stockId);
            throw new RuntimeException("Station non trouvée pour le stock de carburant");
        }
        
        log.debug("Adding {}L to stock ID: {}, current stock: {}, station ID: {}", 
                quantity, stockId, fuelStock.getStockDisponible(), station.getId());
        
        // Ajouter la quantité sans vérification de capacité maximale
        // La capacité maximale représente la capacité de vente journalière, pas la capacité de stockage
        Double newStock = fuelStock.getStockDisponible() + quantity;
        
        fuelStock.setStockDisponible(newStock);
        FuelStock savedStock = fuelStockRepository.save(fuelStock);
        
        // Créer un mouvement d'entrée
        try {
            User currentUser = null;
            try {
                currentUser = authService.getCurrentUser();
                log.debug("Current user for movement: {}", currentUser != null ? currentUser.getEmail() : "null");
            } catch (Exception e) {
                log.warn("Could not get current user for fuel movement: {}", e.getMessage());
            }
            
            FuelMovement.FuelMovementBuilder movementBuilder = FuelMovement.builder()
                    .stock(savedStock)
                    .station(station) // Utiliser la station déjà chargée
                    .type(FuelMovement.MovementType.ENTREE)
                    .quantity(quantity)
                    .prixUnitaire(savedStock.getPrixParLitre());
            
            if (currentUser != null) {
                movementBuilder.createdBy(currentUser);
            }
            
            FuelMovement movement = movementBuilder.build();
            log.debug("Attempting to save fuel movement: type={}, quantity={}, stationId={}, stockId={}", 
                    movement.getType(), movement.getQuantity(), station.getId(), savedStock.getId());
            
            FuelMovement savedMovement = fuelMovementRepository.save(movement);
            // Forcer l'écriture immédiate en base de données
            fuelMovementRepository.flush();
            
            if (savedMovement != null && savedMovement.getId() != null) {
                log.info("Fuel movement entry successfully created with ID: {}, date: {}", 
                        savedMovement.getId(), savedMovement.getDate());
            } else {
                log.error("Fuel movement save returned null or has no ID");
            }
        } catch (Exception e) {
            // Log l'erreur mais ne fait pas échouer l'opération de mise à jour du stock
            log.error("CRITICAL: Error creating fuel movement entry. Stock was updated but movement was not saved.", e);
            log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            // Ne pas propager l'erreur pour que la mise à jour du stock réussisse quand même
        }
        
        return FuelStockDto.fromEntity(savedStock);
    }

    @Transactional
    public FuelStockDto removeFuel(Long stockId, Double quantity) {
        FuelStock fuelStock = fuelStockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock de carburant non trouvé"));
        
        // S'assurer que la station est chargée
        Station station = fuelStock.getStation();
        if (station == null) {
            log.error("Station is null for fuel stock ID: {}", stockId);
            throw new RuntimeException("Station non trouvée pour le stock de carburant");
        }
        
        log.debug("Removing {}L from stock ID: {}, current stock: {}, station ID: {}", 
                quantity, stockId, fuelStock.getStockDisponible(), station.getId());
        
        Double newStock = fuelStock.getStockDisponible() - quantity;
        
        // Vérifier que le stock ne devient pas négatif
        if (newStock < 0) {
            throw new BadRequestException("Quantité insuffisante. Stock disponible: " + fuelStock.getStockDisponible() + "L, tentative de retrait: " + quantity + "L");
        }
        
        fuelStock.setStockDisponible(newStock);
        FuelStock savedStock = fuelStockRepository.save(fuelStock);
        
        // Créer un mouvement de sortie
        try {
            User currentUser = null;
            try {
                currentUser = authService.getCurrentUser();
                log.debug("Current user for movement: {}", currentUser != null ? currentUser.getEmail() : "null");
            } catch (Exception e) {
                log.warn("Could not get current user for fuel movement: {}", e.getMessage());
            }
            
            FuelMovement.FuelMovementBuilder movementBuilder = FuelMovement.builder()
                    .stock(savedStock)
                    .station(station) // Utiliser la station déjà chargée
                    .type(FuelMovement.MovementType.SORTIE)
                    .quantity(quantity)
                    .prixUnitaire(savedStock.getPrixParLitre());
            
            if (currentUser != null) {
                movementBuilder.createdBy(currentUser);
            }
            
            FuelMovement movement = movementBuilder.build();
            log.debug("Attempting to save fuel movement: type={}, quantity={}, stationId={}, stockId={}", 
                    movement.getType(), movement.getQuantity(), station.getId(), savedStock.getId());
            
            FuelMovement savedMovement = fuelMovementRepository.save(movement);
            // Forcer l'écriture immédiate en base de données
            fuelMovementRepository.flush();
            
            if (savedMovement != null && savedMovement.getId() != null) {
                log.info("Fuel movement exit successfully created with ID: {}, date: {}", 
                        savedMovement.getId(), savedMovement.getDate());
            } else {
                log.error("Fuel movement save returned null or has no ID");
            }
        } catch (Exception e) {
            // Log l'erreur mais ne fait pas échouer l'opération de mise à jour du stock
            log.error("CRITICAL: Error creating fuel movement exit. Stock was updated but movement was not saved.", e);
            log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            // Ne pas propager l'erreur pour que la mise à jour du stock réussisse quand même
        }
        
        return FuelStockDto.fromEntity(savedStock);
    }
}

