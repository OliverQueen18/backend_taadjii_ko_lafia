package com.example.fuelticket.controller;

import com.example.fuelticket.dto.CorpsDto;
import com.example.fuelticket.dto.CreateCorpsRequest;
import com.example.fuelticket.service.CorpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/corps")
@RequiredArgsConstructor
public class CorpsController {
    
    private final CorpsService corpsService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<CorpsDto>> getAllCorps() {
        return ResponseEntity.ok(corpsService.getAllCorps());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<CorpsDto> getCorpsById(@PathVariable Long id) {
        return ResponseEntity.ok(corpsService.getCorpsById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<CorpsDto> createCorps(@Valid @RequestBody CreateCorpsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corpsService.createCorps(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<CorpsDto> updateCorps(@PathVariable Long id, @Valid @RequestBody CreateCorpsRequest request) {
        return ResponseEntity.ok(corpsService.updateCorps(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> deleteCorps(@PathVariable Long id) {
        corpsService.deleteCorps(id);
        return ResponseEntity.noContent().build();
    }
}

