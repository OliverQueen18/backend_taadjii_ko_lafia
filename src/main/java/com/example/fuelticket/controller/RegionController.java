package com.example.fuelticket.controller;

import com.example.fuelticket.dto.RegionDto;
import com.example.fuelticket.dto.CreateRegionRequest;
import com.example.fuelticket.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {
    
    private final RegionService regionService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<RegionDto>> getAllRegions() {
        return ResponseEntity.ok(regionService.getAllRegions());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<RegionDto> getRegionById(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.getRegionById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionDto> createRegion(@Valid @RequestBody CreateRegionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regionService.createRegion(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionDto> updateRegion(@PathVariable Long id, @Valid @RequestBody CreateRegionRequest request) {
        return ResponseEntity.ok(regionService.updateRegion(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return ResponseEntity.noContent().build();
    }
}

