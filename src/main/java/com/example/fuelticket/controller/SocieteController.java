package com.example.fuelticket.controller;

import com.example.fuelticket.dto.SocieteDto;
import com.example.fuelticket.dto.CreateSocieteRequest;
import com.example.fuelticket.service.SocieteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/societes")
@RequiredArgsConstructor
public class SocieteController {
    
    private final SocieteService societeService;
    
    @GetMapping
    public ResponseEntity<List<SocieteDto>> getAllSocietes() {
        return ResponseEntity.ok(societeService.getAllSocietes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SocieteDto> getSocieteById(@PathVariable Long id) {
        return ResponseEntity.ok(societeService.getSocieteById(id));
    }
    
    @PostMapping
    public ResponseEntity<SocieteDto> createSociete(@Valid @RequestBody CreateSocieteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(societeService.createSociete(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SocieteDto> updateSociete(@PathVariable Long id, @Valid @RequestBody CreateSocieteRequest request) {
        return ResponseEntity.ok(societeService.updateSociete(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSociete(@PathVariable Long id) {
        societeService.deleteSociete(id);
        return ResponseEntity.noContent().build();
    }
}

