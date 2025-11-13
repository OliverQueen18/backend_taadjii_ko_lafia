package com.example.fuelticket.controller;

import com.example.fuelticket.dto.ApprovisionnementDto;
import com.example.fuelticket.dto.CreateApprovisionnementRequest;
import com.example.fuelticket.service.ApprovisionnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/approvisionnements")
@RequiredArgsConstructor
public class ApprovisionnementController {
    
    private final ApprovisionnementService approvisionnementService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<ApprovisionnementDto>> getAllApprovisionnements() {
        return ResponseEntity.ok(approvisionnementService.getAllApprovisionnements());
    }
    
    @GetMapping("/societe/{societeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<ApprovisionnementDto>> getApprovisionnementsBySociete(@PathVariable Long societeId) {
        return ResponseEntity.ok(approvisionnementService.getApprovisionnementsBySociete(societeId));
    }
    
    @GetMapping("/region/{regionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<ApprovisionnementDto>> getApprovisionnementsByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(approvisionnementService.getApprovisionnementsByRegion(regionId));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<ApprovisionnementDto> getApprovisionnementById(@PathVariable Long id) {
        return ResponseEntity.ok(approvisionnementService.getApprovisionnementById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<ApprovisionnementDto> createApprovisionnement(@Valid @RequestBody CreateApprovisionnementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(approvisionnementService.createApprovisionnement(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<ApprovisionnementDto> updateApprovisionnement(@PathVariable Long id, @Valid @RequestBody CreateApprovisionnementRequest request) {
        return ResponseEntity.ok(approvisionnementService.updateApprovisionnement(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> deleteApprovisionnement(@PathVariable Long id) {
        approvisionnementService.deleteApprovisionnement(id);
        return ResponseEntity.noContent().build();
    }
}

