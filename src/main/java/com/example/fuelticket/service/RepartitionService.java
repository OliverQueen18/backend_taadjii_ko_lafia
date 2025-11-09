package com.example.fuelticket.service;

import com.example.fuelticket.dto.RepartitionDto;
import com.example.fuelticket.dto.CreateRepartitionRequest;
import com.example.fuelticket.entity.Repartition;
import com.example.fuelticket.entity.Corps;
import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.repository.RepartitionRepository;
import com.example.fuelticket.repository.CorpsRepository;
import com.example.fuelticket.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepartitionService {
    
    private final RepartitionRepository repartitionRepository;
    private final CorpsRepository corpsRepository;
    private final StationRepository stationRepository;
    private final AuthService authService;
    
    public List<RepartitionDto> getAllRepartitions() {
        return repartitionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RepartitionDto> getRepartitionsByCorps(Long corpsId) {
        Corps corps = corpsRepository.findById(corpsId)
                .orElseThrow(() -> new RuntimeException("Corps non trouvé avec l'id: " + corpsId));
        return repartitionRepository.findByCorps(corps).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RepartitionDto> getRepartitionsByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée avec l'id: " + stationId));
        return repartitionRepository.findByStation(station).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RepartitionDto getRepartitionById(Long id) {
        Repartition repartition = repartitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Répartition non trouvée avec l'id: " + id));
        return convertToDto(repartition);
    }
    
    @Transactional
    public RepartitionDto createRepartition(CreateRepartitionRequest request) {
        // Vérifier que l'utilisateur est GESTIONNAIRE ou ADMIN
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.GESTIONNAIRE && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de créer une répartition");
        }
        
        Corps corps = corpsRepository.findById(request.getCorpsId())
                .orElseThrow(() -> new RuntimeException("Corps non trouvé avec l'id: " + request.getCorpsId()));
        
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station non trouvée avec l'id: " + request.getStationId()));
        
        // Vérifier si la répartition existe déjà
        if (repartitionRepository.existsByCorpsAndStation(corps, station)) {
            throw new RuntimeException("Cette répartition existe déjà");
        }
        
        Repartition repartition = Repartition.builder()
                .corps(corps)
                .station(station)
                .commentaire(request.getCommentaire())
                .build();
        
        Repartition savedRepartition = repartitionRepository.save(repartition);
        return convertToDto(savedRepartition);
    }
    
    @Transactional
    public RepartitionDto updateRepartition(Long id, CreateRepartitionRequest request) {
        // Vérifier que l'utilisateur est GESTIONNAIRE ou ADMIN
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.GESTIONNAIRE && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de modifier une répartition");
        }
        
        Repartition repartition = repartitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Répartition non trouvée avec l'id: " + id));
        
        Corps corps = corpsRepository.findById(request.getCorpsId())
                .orElseThrow(() -> new RuntimeException("Corps non trouvé avec l'id: " + request.getCorpsId()));
        
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station non trouvée avec l'id: " + request.getStationId()));
        
        // Vérifier si une autre répartition avec le même corps et station existe déjà
        repartitionRepository.findByCorpsAndStation(corps, station)
                .ifPresent(existingRepartition -> {
                    if (!existingRepartition.getId().equals(id)) {
                        throw new RuntimeException("Cette répartition existe déjà");
                    }
                });
        
        repartition.setCorps(corps);
        repartition.setStation(station);
        repartition.setCommentaire(request.getCommentaire());
        
        Repartition savedRepartition = repartitionRepository.save(repartition);
        return convertToDto(savedRepartition);
    }
    
    @Transactional
    public void deleteRepartition(Long id) {
        // Vérifier que l'utilisateur est GESTIONNAIRE ou ADMIN
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.GESTIONNAIRE && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de supprimer une répartition");
        }
        
        repartitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Répartition non trouvée avec l'id: " + id));
        
        repartitionRepository.deleteById(id);
    }
    
    private RepartitionDto convertToDto(Repartition repartition) {
        RepartitionDto dto = new RepartitionDto();
        dto.setId(repartition.getId());
        dto.setCorpsId(repartition.getCorps().getId());
        dto.setCorpsNom(repartition.getCorps().getNom());
        dto.setStationId(repartition.getStation().getId());
        dto.setStationNom(repartition.getStation().getNom());
        dto.setCommentaire(repartition.getCommentaire());
        return dto;
    }
}

