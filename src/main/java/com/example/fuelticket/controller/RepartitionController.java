package com.example.fuelticket.controller;

import com.example.fuelticket.dto.RepartitionDto;
import com.example.fuelticket.dto.CreateRepartitionRequest;
import com.example.fuelticket.service.RepartitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/repartitions")
@RequiredArgsConstructor
public class RepartitionController {
    
    private final RepartitionService repartitionService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<RepartitionDto>> getAllRepartitions() {
        return ResponseEntity.ok(repartitionService.getAllRepartitions());
    }
    
    @GetMapping("/corps/{corpsId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<RepartitionDto>> getRepartitionsByCorps(@PathVariable Long corpsId) {
        return ResponseEntity.ok(repartitionService.getRepartitionsByCorps(corpsId));
    }
    
    @GetMapping("/station/{stationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE') or hasRole('STATION')")
    public ResponseEntity<List<RepartitionDto>> getRepartitionsByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(repartitionService.getRepartitionsByStation(stationId));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<RepartitionDto> getRepartitionById(@PathVariable Long id) {
        return ResponseEntity.ok(repartitionService.getRepartitionById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<RepartitionDto> createRepartition(@Valid @RequestBody CreateRepartitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repartitionService.createRepartition(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<RepartitionDto> updateRepartition(@PathVariable Long id, @Valid @RequestBody CreateRepartitionRequest request) {
        return ResponseEntity.ok(repartitionService.updateRepartition(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> deleteRepartition(@PathVariable Long id) {
        repartitionService.deleteRepartition(id);
        return ResponseEntity.noContent().build();
    }
}

