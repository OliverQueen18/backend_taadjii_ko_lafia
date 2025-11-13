package com.example.fuelticket.service;

import com.example.fuelticket.dto.ApprovisionnementDto;
import com.example.fuelticket.dto.CreateApprovisionnementRequest;
import com.example.fuelticket.entity.Approvisionnement;
import com.example.fuelticket.entity.Societe;
import com.example.fuelticket.entity.Region;
import com.example.fuelticket.entity.FuelStock;
import com.example.fuelticket.repository.ApprovisionnementRepository;
import com.example.fuelticket.repository.SocieteRepository;
import com.example.fuelticket.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovisionnementService {
    
    private final ApprovisionnementRepository approvisionnementRepository;
    private final SocieteRepository societeRepository;
    private final RegionRepository regionRepository;
    
    public List<ApprovisionnementDto> getAllApprovisionnements() {
        return approvisionnementRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ApprovisionnementDto> getApprovisionnementsBySociete(Long societeId) {
        Societe societe = societeRepository.findById(societeId)
                .orElseThrow(() -> new RuntimeException("Société non trouvée avec l'id: " + societeId));
        return approvisionnementRepository.findBySociete(societe).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ApprovisionnementDto> getApprovisionnementsByRegion(Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + regionId));
        return approvisionnementRepository.findByRegion(region).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ApprovisionnementDto getApprovisionnementById(Long id) {
        Approvisionnement approvisionnement = approvisionnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approvisionnement non trouvé avec l'id: " + id));
        return convertToDto(approvisionnement);
    }
    
    @Transactional
    public ApprovisionnementDto createApprovisionnement(CreateApprovisionnementRequest request) {
        Societe societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new RuntimeException("Société non trouvée avec l'id: " + request.getSocieteId()));
        
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + request.getRegionId()));
        
        Approvisionnement approvisionnement = Approvisionnement.builder()
                .societe(societe)
                .region(region)
                .fuelType(request.getFuelType())
                .quantite(request.getQuantite())
                .dateApprovisionnement(request.getDateApprovisionnement() != null ? 
                        request.getDateApprovisionnement() : LocalDateTime.now())
                .numeroCiterne(request.getNumeroCiterne())
                .numeroBonLivraison(request.getNumeroBonLivraison())
                .commentaire(request.getCommentaire())
                .build();
        
        Approvisionnement savedApprovisionnement = approvisionnementRepository.save(approvisionnement);
        return convertToDto(savedApprovisionnement);
    }
    
    @Transactional
    public ApprovisionnementDto updateApprovisionnement(Long id, CreateApprovisionnementRequest request) {
        Approvisionnement approvisionnement = approvisionnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approvisionnement non trouvé avec l'id: " + id));
        
        Societe societe = societeRepository.findById(request.getSocieteId())
                .orElseThrow(() -> new RuntimeException("Société non trouvée avec l'id: " + request.getSocieteId()));
        
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + request.getRegionId()));
        
        approvisionnement.setSociete(societe);
        approvisionnement.setRegion(region);
        approvisionnement.setFuelType(request.getFuelType());
        approvisionnement.setQuantite(request.getQuantite());
        approvisionnement.setDateApprovisionnement(request.getDateApprovisionnement());
        approvisionnement.setNumeroCiterne(request.getNumeroCiterne());
        approvisionnement.setNumeroBonLivraison(request.getNumeroBonLivraison());
        approvisionnement.setCommentaire(request.getCommentaire());
        
        Approvisionnement savedApprovisionnement = approvisionnementRepository.save(approvisionnement);
        return convertToDto(savedApprovisionnement);
    }
    
    @Transactional
    public void deleteApprovisionnement(Long id) {
        approvisionnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approvisionnement non trouvé avec l'id: " + id));
        
        approvisionnementRepository.deleteById(id);
    }
    
    private ApprovisionnementDto convertToDto(Approvisionnement approvisionnement) {
        ApprovisionnementDto dto = new ApprovisionnementDto();
        dto.setId(approvisionnement.getId());
        dto.setSocieteId(approvisionnement.getSociete().getId());
        dto.setSocieteNom(approvisionnement.getSociete().getNom());
        dto.setRegionId(approvisionnement.getRegion().getId());
        dto.setRegionNom(approvisionnement.getRegion().getNom());
        dto.setRegionCode(approvisionnement.getRegion().getCode());
        dto.setFuelType(approvisionnement.getFuelType());
        dto.setFuelTypeDisplayName(approvisionnement.getFuelType().getDisplayName());
        dto.setQuantite(approvisionnement.getQuantite());
        dto.setDateApprovisionnement(approvisionnement.getDateApprovisionnement());
        dto.setNumeroCiterne(approvisionnement.getNumeroCiterne());
        dto.setNumeroBonLivraison(approvisionnement.getNumeroBonLivraison());
        dto.setCommentaire(approvisionnement.getCommentaire());
        dto.setCreatedAt(approvisionnement.getCreatedAt());
        return dto;
    }
}

