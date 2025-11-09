package com.example.fuelticket.controller;

import com.example.fuelticket.dto.ApprovisionnementDto;
import com.example.fuelticket.dto.CreateApprovisionnementRequest;
import com.example.fuelticket.service.ApprovisionnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/approvisionnements")
@RequiredArgsConstructor
public class ApprovisionnementController {
    
    private final ApprovisionnementService approvisionnementService;
    
    @GetMapping
    public ResponseEntity<List<ApprovisionnementDto>> getAllApprovisionnements() {
        return ResponseEntity.ok(approvisionnementService.getAllApprovisionnements());
    }
    
    @GetMapping("/societe/{societeId}")
    public ResponseEntity<List<ApprovisionnementDto>> getApprovisionnementsBySociete(@PathVariable Long societeId) {
        return ResponseEntity.ok(approvisionnementService.getApprovisionnementsBySociete(societeId));
    }
    
    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<ApprovisionnementDto>> getApprovisionnementsByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(approvisionnementService.getApprovisionnementsByStation(stationId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApprovisionnementDto> getApprovisionnementById(@PathVariable Long id) {
        return ResponseEntity.ok(approvisionnementService.getApprovisionnementById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApprovisionnementDto> createApprovisionnement(@Valid @RequestBody CreateApprovisionnementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(approvisionnementService.createApprovisionnement(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApprovisionnementDto> updateApprovisionnement(@PathVariable Long id, @Valid @RequestBody CreateApprovisionnementRequest request) {
        return ResponseEntity.ok(approvisionnementService.updateApprovisionnement(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApprovisionnement(@PathVariable Long id) {
        approvisionnementService.deleteApprovisionnement(id);
        return ResponseEntity.noContent().build();
    }
}

