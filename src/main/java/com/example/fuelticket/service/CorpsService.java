package com.example.fuelticket.service;

import com.example.fuelticket.dto.CorpsDto;
import com.example.fuelticket.dto.CreateCorpsRequest;
import com.example.fuelticket.entity.Corps;
import com.example.fuelticket.entity.User;
import com.example.fuelticket.repository.CorpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorpsService {
    
    private final CorpsRepository corpsRepository;
    private final AuthService authService;
    
    public List<CorpsDto> getAllCorps() {
        return corpsRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public CorpsDto getCorpsById(Long id) {
        Corps corps = corpsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corps non trouvé avec l'id: " + id));
        return convertToDto(corps);
    }
    
    @Transactional
    public CorpsDto createCorps(CreateCorpsRequest request) {
        // Vérifier que l'utilisateur est GESTIONNAIRE ou ADMIN
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.GESTIONNAIRE && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de créer un corps");
        }
        
        // Vérifier si un corps avec le même nom existe déjà
        if (corpsRepository.findByNom(request.getNom()).isPresent()) {
            throw new RuntimeException("Un corps avec ce nom existe déjà");
        }
        
        Corps corps = Corps.builder()
                .nom(request.getNom())
                .detail(request.getDetail())
                .build();
        
        Corps savedCorps = corpsRepository.save(corps);
        return convertToDto(savedCorps);
    }
    
    @Transactional
    public CorpsDto updateCorps(Long id, CreateCorpsRequest request) {
        // Vérifier que l'utilisateur est GESTIONNAIRE ou ADMIN
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.GESTIONNAIRE && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de modifier un corps");
        }
        
        Corps corps = corpsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corps non trouvé avec l'id: " + id));
        
        // Vérifier si un autre corps avec le même nom existe déjà
        corpsRepository.findByNom(request.getNom())
                .ifPresent(existingCorps -> {
                    if (!existingCorps.getId().equals(id)) {
                        throw new RuntimeException("Un corps avec ce nom existe déjà");
                    }
                });
        
        corps.setNom(request.getNom());
        corps.setDetail(request.getDetail());
        
        Corps savedCorps = corpsRepository.save(corps);
        return convertToDto(savedCorps);
    }
    
    @Transactional
    public void deleteCorps(Long id) {
        // Vérifier que l'utilisateur est GESTIONNAIRE ou ADMIN
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.GESTIONNAIRE && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Vous n'avez pas l'autorisation de supprimer un corps");
        }
        
        Corps corps = corpsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corps non trouvé avec l'id: " + id));
        
        // Vérifier s'il y a des répartitions associées
        if (corps.getRepartitions() != null && !corps.getRepartitions().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer le corps. Il est associé à " + 
                    corps.getRepartitions().size() + " répartition(s).");
        }
        
        corpsRepository.deleteById(id);
    }
    
    private CorpsDto convertToDto(Corps corps) {
        CorpsDto dto = new CorpsDto();
        dto.setId(corps.getId());
        dto.setNom(corps.getNom());
        dto.setDetail(corps.getDetail());
        return dto;
    }
}

