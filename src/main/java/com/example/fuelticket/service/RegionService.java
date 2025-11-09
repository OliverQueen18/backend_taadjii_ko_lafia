package com.example.fuelticket.service;

import com.example.fuelticket.dto.RegionDto;
import com.example.fuelticket.dto.CreateRegionRequest;
import com.example.fuelticket.entity.Region;
import com.example.fuelticket.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionService {
    
    private final RegionRepository regionRepository;
    
    public List<RegionDto> getAllRegions() {
        return regionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RegionDto getRegionById(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + id));
        return convertToDto(region);
    }
    
    @Transactional
    public RegionDto createRegion(CreateRegionRequest request) {
        // Vérifier si une région avec le même code existe déjà
        if (regionRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Une région avec ce code existe déjà");
        }
        
        Region region = Region.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        
        Region savedRegion = regionRepository.save(region);
        return convertToDto(savedRegion);
    }
    
    @Transactional
    public RegionDto updateRegion(Long id, CreateRegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + id));
        
        // Vérifier si une autre région avec le même code existe déjà
        regionRepository.findByCode(request.getCode())
                .ifPresent(existingRegion -> {
                    if (!existingRegion.getId().equals(id)) {
                        throw new RuntimeException("Une région avec ce code existe déjà");
                    }
                });
        
        region.setCode(request.getCode());
        region.setNom(request.getNom());
        region.setLatitude(request.getLatitude());
        region.setLongitude(request.getLongitude());
        
        Region savedRegion = regionRepository.save(region);
        return convertToDto(savedRegion);
    }
    
    @Transactional
    public void deleteRegion(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id: " + id));
        
        // Vérifier s'il y a des stations associées
        if (region.getStations() != null && !region.getStations().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer la région. Elle est associée à " + 
                    region.getStations().size() + " station(s).");
        }
        
        regionRepository.deleteById(id);
    }
    
    private RegionDto convertToDto(Region region) {
        RegionDto dto = new RegionDto();
        dto.setId(region.getId());
        dto.setCode(region.getCode());
        dto.setNom(region.getNom());
        dto.setLatitude(region.getLatitude());
        dto.setLongitude(region.getLongitude());
        return dto;
    }
}

