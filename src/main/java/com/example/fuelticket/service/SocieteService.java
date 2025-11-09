package com.example.fuelticket.service;

import com.example.fuelticket.dto.SocieteDto;
import com.example.fuelticket.dto.CreateSocieteRequest;
import com.example.fuelticket.entity.Societe;
import com.example.fuelticket.repository.SocieteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocieteService {
    
    private final SocieteRepository societeRepository;
    
    public List<SocieteDto> getAllSocietes() {
        return societeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public SocieteDto getSocieteById(Long id) {
        Societe societe = societeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Société non trouvée avec l'id: " + id));
        return convertToDto(societe);
    }
    
    @Transactional
    public SocieteDto createSociete(CreateSocieteRequest request) {
        // Vérifier si une société avec le même nom existe déjà
        if (societeRepository.findByNom(request.getNom()).isPresent()) {
            throw new RuntimeException("Une société avec ce nom existe déjà");
        }
        
        Societe societe = Societe.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .siteWeb(request.getSiteWeb())
                .logoUrl(request.getLogoUrl())
                .build();
        
        Societe savedSociete = societeRepository.save(societe);
        return convertToDto(savedSociete);
    }
    
    @Transactional
    public SocieteDto updateSociete(Long id, CreateSocieteRequest request) {
        Societe societe = societeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Société non trouvée avec l'id: " + id));
        
        // Vérifier si une autre société avec le même nom existe déjà
        societeRepository.findByNom(request.getNom())
                .ifPresent(existingSociete -> {
                    if (!existingSociete.getId().equals(id)) {
                        throw new RuntimeException("Une société avec ce nom existe déjà");
                    }
                });
        
        societe.setNom(request.getNom());
        societe.setDescription(request.getDescription());
        societe.setAdresse(request.getAdresse());
        societe.setTelephone(request.getTelephone());
        societe.setEmail(request.getEmail());
        societe.setSiteWeb(request.getSiteWeb());
        societe.setLogoUrl(request.getLogoUrl());
        
        Societe savedSociete = societeRepository.save(societe);
        return convertToDto(savedSociete);
    }
    
    @Transactional
    public void deleteSociete(Long id) {
        Societe societe = societeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Société non trouvée avec l'id: " + id));
        
        // Vérifier s'il y a des approvisionnements associés
        if (societe.getApprovisionnements() != null && !societe.getApprovisionnements().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer la société. Elle est associée à " + 
                    societe.getApprovisionnements().size() + " approvisionnement(s).");
        }
        
        societeRepository.deleteById(id);
    }
    
    private SocieteDto convertToDto(Societe societe) {
        SocieteDto dto = new SocieteDto();
        dto.setId(societe.getId());
        dto.setNom(societe.getNom());
        dto.setDescription(societe.getDescription());
        dto.setAdresse(societe.getAdresse());
        dto.setTelephone(societe.getTelephone());
        dto.setEmail(societe.getEmail());
        dto.setSiteWeb(societe.getSiteWeb());
        dto.setLogoUrl(societe.getLogoUrl());
        dto.setCreatedAt(societe.getCreatedAt());
        dto.setUpdatedAt(societe.getUpdatedAt());
        return dto;
    }
}

