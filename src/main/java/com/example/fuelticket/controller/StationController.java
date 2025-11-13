package com.example.fuelticket.controller;

import com.example.fuelticket.dto.StationDto;
import com.example.fuelticket.dto.StationWithStocksDto;
import com.example.fuelticket.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@Tag(name = "Stations", description = "Station management APIs")
public class StationController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Get all stations", description = "Retrieve all stations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE') or hasRole('STATION')")
    public ResponseEntity<List<StationDto>> getAllStations() {
        List<StationDto> stations = stationService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/with-stocks")
    @Operation(summary = "Get all stations with stocks", description = "Retrieve all stations with their fuel stocks. For STATION role, returns only manager's stations.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE') or hasRole('STATION')")
    public ResponseEntity<List<StationWithStocksDto>> getAllStationsWithStocks() {
        List<StationWithStocksDto> stations = stationService.getAllStationsWithStocks();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/my-stations")
    @Operation(summary = "Get my stations with stocks", description = "Retrieve stations managed by the current authenticated user")
    @PreAuthorize("hasRole('STATION')")
    public ResponseEntity<List<StationWithStocksDto>> getMyStationsWithStocks() {
        List<StationWithStocksDto> stations = stationService.getMyStationsWithStocks();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get station by ID", description = "Retrieve a station by its ID")
    public ResponseEntity<StationDto> getStationById(@PathVariable Long id) {
        StationDto station = stationService.getStationById(id);
        return ResponseEntity.ok(station);
    }

    @GetMapping("/{id}/with-stocks")
    @Operation(summary = "Get station with stocks by ID", description = "Retrieve a station with its fuel stocks by ID")
    public ResponseEntity<StationWithStocksDto> getStationWithStocksById(@PathVariable Long id) {
        StationWithStocksDto station = stationService.getStationWithStocksById(id);
        return ResponseEntity.ok(station);
    }

    @PostMapping
    @Operation(summary = "Create station", description = "Create a new station")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STATION')")
    public ResponseEntity<StationDto> createStation(@Valid @RequestBody StationDto stationDto) {
        StationDto createdStation = stationService.createStation(stationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update station", description = "Update an existing station")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STATION')")
    public ResponseEntity<StationDto> updateStation(@PathVariable Long id, @Valid @RequestBody StationDto stationDto) {
        StationDto updatedStation = stationService.updateStation(id, stationDto);
        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete station", description = "Delete a station by ID. ADMIN can delete any station, STATION can only delete their own stations.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STATION')")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}
