package com.example.fuelticket.service;

import com.example.fuelticket.dto.CreateSaleScheduleRequest;
import com.example.fuelticket.dto.SaleScheduleDto;
import com.example.fuelticket.entity.SaleSchedule;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.repository.SaleScheduleRepository;
import com.example.fuelticket.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleScheduleService {
    
    private final SaleScheduleRepository saleScheduleRepository;
    private final StationRepository stationRepository;
    
    @Transactional
    @CacheEvict(value = "saleSchedules", allEntries = true)
    public SaleScheduleDto createSaleSchedule(CreateSaleScheduleRequest request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        // Vérifier s'il existe déjà une planification pour cette station, date et type de carburant
        if (saleScheduleRepository.existsByStationAndSaleDateAndFuelTypeAndIsActiveTrue(
                station, request.getSaleDate(), request.getFuelType())) {
            throw new RuntimeException("Une planification existe déjà pour cette station, date et type de carburant");
        }
        
        // Vérifier que l'heure de fin est après l'heure de début
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().equals(request.getStartTime())) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début");
        }
        
        // Vérifier que la quantité disponible ne dépasse pas la capacité journalière
        if (request.getAvailableQuantity() > station.getCapaciteJournaliere()) {
            throw new RuntimeException("La quantité disponible ne peut pas dépasser la capacité journalière de la station");
        }
        
        SaleSchedule schedule = SaleSchedule.builder()
                .station(station)
                .fuelType(request.getFuelType())
                .saleDate(request.getSaleDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .availableQuantity(request.getAvailableQuantity())
                .maxQuantityPerTicket(request.getMaxQuantityPerTicket())
                .maxTicketsPerDay(request.getMaxTicketsPerDay())
                .isActive(request.getIsActive())
                .build();
        
        SaleSchedule savedSchedule = saleScheduleRepository.save(schedule);
        return SaleScheduleDto.fromEntity(savedSchedule);
    }
    
    @Cacheable(value = "saleSchedules", key = "'station-' + #stationId")
    public List<SaleScheduleDto> getSaleSchedulesByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        // Retourner tous les schedules (actifs et inactifs) pour permettre l'affichage de toutes les données
        return saleScheduleRepository.findByStation(station).stream()
                .map(SaleScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<SaleScheduleDto> getAllSaleSchedulesByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return saleScheduleRepository.findByStation(station).stream()
                .map(SaleScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "saleSchedules", key = "'all'")
    public List<SaleScheduleDto> getAllSaleSchedules() {
        // Utiliser une requête avec JOIN FETCH pour éviter LazyInitializationException
        return saleScheduleRepository.findAllWithRelations().stream()
                .map(schedule -> {
                    try {
                        return SaleScheduleDto.fromEntity(schedule);
                    } catch (Exception e) {
                        // En cas d'erreur (ex: LazyInitializationException), créer un DTO basique
                        System.err.println("Erreur lors de la conversion du schedule: " + e.getMessage());
                        SaleScheduleDto dto = new SaleScheduleDto();
                        dto.setId(schedule.getId());
                        dto.setStationId(schedule.getStation() != null ? schedule.getStation().getId() : null);
                        dto.setStationName(schedule.getStation() != null ? schedule.getStation().getNom() : null);
                        dto.setFuelType(schedule.getFuelType());
                        dto.setFuelTypeDisplayName(schedule.getFuelType() != null ? schedule.getFuelType().getDisplayName() : null);
                        dto.setSaleDate(schedule.getSaleDate());
                        dto.setStartTime(schedule.getStartTime());
                        dto.setEndTime(schedule.getEndTime());
                        dto.setAvailableQuantity(schedule.getAvailableQuantity());
                        dto.setMaxQuantityPerTicket(schedule.getMaxQuantityPerTicket());
                        dto.setMaxTicketsPerDay(schedule.getMaxTicketsPerDay());
                        dto.setIsActive(schedule.getIsActive());
                        // Utiliser les valeurs de base si les méthodes calculées échouent
                        try {
                            dto.setRemainingQuantity(schedule.getRemainingQuantity());
                        } catch (Exception ex) {
                            dto.setRemainingQuantity(schedule.getAvailableQuantity());
                        }
                        try {
                            dto.setRemainingTickets(schedule.getRemainingTickets());
                        } catch (Exception ex) {
                            dto.setRemainingTickets(schedule.getMaxTicketsPerDay());
                        }
                        return dto;
                    }
                })
                .collect(Collectors.toList());
    }
    
    public List<SaleScheduleDto> getSaleSchedulesByStationAndDate(Long stationId, LocalDate date) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return saleScheduleRepository.findByStationAndSaleDateAndIsActiveTrue(station, date).stream()
                .map(SaleScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<SaleScheduleDto> getSaleSchedulesByStationAndDateRange(Long stationId, LocalDate startDate, LocalDate endDate) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        return saleScheduleRepository.findActiveSchedulesByStationAndDateRange(station, startDate, endDate).stream()
                .map(SaleScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(value = "saleSchedules", allEntries = true)
    public SaleScheduleDto updateSaleSchedule(Long scheduleId, CreateSaleScheduleRequest request) {
        SaleSchedule schedule = saleScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Planification non trouvée"));
        
        // Vérifier que la planification n'a pas encore de tickets
        if (schedule.getTickets() != null && !schedule.getTickets().isEmpty()) {
            throw new RuntimeException("Impossible de modifier une planification qui a déjà des tickets");
        }
        
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        schedule.setStation(station);
        schedule.setFuelType(request.getFuelType());
        schedule.setSaleDate(request.getSaleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setAvailableQuantity(request.getAvailableQuantity());
        schedule.setMaxQuantityPerTicket(request.getMaxQuantityPerTicket());
        schedule.setMaxTicketsPerDay(request.getMaxTicketsPerDay());
        schedule.setIsActive(request.getIsActive());
        
        SaleSchedule savedSchedule = saleScheduleRepository.save(schedule);
        return SaleScheduleDto.fromEntity(savedSchedule);
    }
    
    @Transactional
    @CacheEvict(value = "saleSchedules", allEntries = true)
    public void deleteSaleSchedule(Long scheduleId) {
        SaleSchedule schedule = saleScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Planification non trouvée"));
        
        // Vérifier que la planification n'a pas encore de tickets
        if (schedule.getTickets() != null && !schedule.getTickets().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une planification qui a déjà des tickets");
        }
        
        saleScheduleRepository.delete(schedule);
    }
    
    @Transactional
    @CacheEvict(value = "saleSchedules", allEntries = true)
    public SaleScheduleDto toggleScheduleStatus(Long scheduleId) {
        SaleSchedule schedule = saleScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Planification non trouvée"));
        
        schedule.setIsActive(!schedule.getIsActive());
        SaleSchedule savedSchedule = saleScheduleRepository.save(schedule);
        return SaleScheduleDto.fromEntity(savedSchedule);
    }
    
    @Transactional(readOnly = true)
    public List<SaleScheduleDto> getAvailableSchedulesForTicket(Long stationId, LocalDate date, SaleSchedule.FuelType fuelType) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));
        
        // Utiliser la requête avec JOIN FETCH pour charger les tickets
        List<SaleSchedule> schedules = saleScheduleRepository.findActiveSchedulesByStationDateAndFuelTypeWithTickets(station, date, fuelType);
        
        return schedules.stream()
                .filter(SaleSchedule::isAvailableForSale)
                .map(schedule -> {
                    try {
                        return SaleScheduleDto.fromEntity(schedule);
                    } catch (Exception e) {
                        // En cas d'erreur (ex: LazyInitializationException), créer un DTO basique
                        System.err.println("Erreur lors de la conversion du schedule: " + e.getMessage());
                        SaleScheduleDto dto = new SaleScheduleDto();
                        dto.setId(schedule.getId());
                        dto.setStationId(station.getId());
                        dto.setStationName(station.getNom());
                        dto.setFuelType(schedule.getFuelType());
                        dto.setFuelTypeDisplayName(schedule.getFuelType() != null ? schedule.getFuelType().getDisplayName() : null);
                        dto.setSaleDate(schedule.getSaleDate());
                        dto.setStartTime(schedule.getStartTime());
                        dto.setEndTime(schedule.getEndTime());
                        dto.setAvailableQuantity(schedule.getAvailableQuantity());
                        dto.setMaxQuantityPerTicket(schedule.getMaxQuantityPerTicket());
                        dto.setMaxTicketsPerDay(schedule.getMaxTicketsPerDay());
                        dto.setIsActive(schedule.getIsActive());
                        // Utiliser les valeurs de base si les méthodes calculées échouent
                        dto.setRemainingQuantity(schedule.getAvailableQuantity());
                        dto.setRemainingTickets(schedule.getMaxTicketsPerDay());
                        return dto;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère les dates planifiées futures (>= aujourd'hui) pour une station
     * @param stationId ID de la station
     * @return Liste des dates distinctes planifiées (>= aujourd'hui)
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getPlannedDatesForStation(Long stationId) {
        try {
            Station station = stationRepository.findById(stationId)
                    .orElseThrow(() -> new RuntimeException("Station non trouvée avec l'ID: " + stationId));
            
            LocalDate today = LocalDate.now();
            
            List<SaleSchedule> schedules = saleScheduleRepository.findByStationAndIsActiveTrue(station);
            
            return schedules.stream()
                    .map(schedule -> {
                        try {
                            return schedule.getSaleDate();
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'accès à saleDate: " + e.getMessage());
                            return (LocalDate) null;
                        }
                    })
                    .filter(date -> date != null && (date.isEqual(today) || date.isAfter(today)))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur dans getPlannedDatesForStation pour stationId=" + stationId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des dates planifiées: " + e.getMessage(), e);
        }
    }
}
