package com.example.fuelticket.controller;

import com.example.fuelticket.dto.FuelMovementDto;
import com.example.fuelticket.entity.FuelMovement;
import com.example.fuelticket.service.FuelMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fuel-movements")
@RequiredArgsConstructor
@Tag(name = "Fuel Movements", description = "Fuel movement management APIs")
public class FuelMovementController {
    
    private final FuelMovementService fuelMovementService;

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get movements by station", description = "Get all fuel movements for a specific station, ordered by date descending")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<FuelMovementDto>> getMovementsByStation(@PathVariable Long stationId) {
        List<FuelMovementDto> movements = fuelMovementService.getMovementsByStation(stationId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/station/{stationId}/range")
    @Operation(summary = "Get movements by station and date range", description = "Get fuel movements for a station within a date range")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<FuelMovementDto>> getMovementsByStationAndDateRange(
            @PathVariable Long stationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FuelMovementDto> movements = fuelMovementService.getMovementsByStationAndDateRange(stationId, startDate, endDate);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/my-stations")
    @Operation(summary = "Get movements for my stations", description = "Get fuel movements for all stations managed by the current user (STATION role)")
    @PreAuthorize("hasRole('STATION')")
    public ResponseEntity<List<FuelMovementDto>> getMovementsByMyStations() {
        List<FuelMovementDto> movements = fuelMovementService.getMovementsByMyStations();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/stock/{stockId}")
    @Operation(summary = "Get movements by stock", description = "Get all fuel movements for a specific fuel stock")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<FuelMovementDto>> getMovementsByStock(@PathVariable Long stockId) {
        List<FuelMovementDto> movements = fuelMovementService.getMovementsByStock(stockId);
        return ResponseEntity.ok(movements);
    }

    @PostMapping
    @Operation(summary = "Create movement", description = "Create a new fuel movement")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<FuelMovementDto> createMovement(
            @RequestParam Long stockId,
            @RequestParam FuelMovement.MovementType type,
            @RequestParam Double quantity,
            @RequestParam(required = false) String description) {
        FuelMovementDto movement = fuelMovementService.createMovement(stockId, type, quantity, description);
        return ResponseEntity.ok(movement);
    }
}

