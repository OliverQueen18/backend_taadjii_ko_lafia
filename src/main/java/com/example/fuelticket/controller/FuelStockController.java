package com.example.fuelticket.controller;

import com.example.fuelticket.dto.FuelStockDto;
import com.example.fuelticket.entity.FuelStock;
import com.example.fuelticket.service.FuelStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fuel-stocks")
@RequiredArgsConstructor
@Tag(name = "Fuel Stocks", description = "Fuel stock management APIs")
public class FuelStockController {
    
    private final FuelStockService fuelStockService;

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get fuel stocks by station", description = "Get all fuel stocks for a specific station")
    public ResponseEntity<List<FuelStockDto>> getFuelStocksByStation(@PathVariable Long stationId) {
        List<FuelStockDto> stocks = fuelStockService.getFuelStocksByStation(stationId);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/station/{stationId}/available")
    @Operation(summary = "Get available fuel stocks by station", description = "Get only available fuel stocks for a specific station")
    public ResponseEntity<List<FuelStockDto>> getAvailableFuelStocksByStation(@PathVariable Long stationId) {
        List<FuelStockDto> stocks = fuelStockService.getAvailableFuelStocksByStation(stationId);
        return ResponseEntity.ok(stocks);
    }

    @PutMapping("/{stockId}")
    @Operation(summary = "Update fuel stock", description = "Update fuel stock quantity and price")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<FuelStockDto> updateFuelStock(
            @PathVariable @NotNull Long stockId,
            @RequestParam @NotNull @Min(value = 0, message = "Le stock ne peut pas être négatif") Double newStock,
            @RequestParam(required = false) @Min(value = 0, message = "Le prix ne peut pas être négatif") Double newPrice) {
        log.info("Updating stock ID: {} with new stock: {}L", stockId, newStock);
        FuelStockDto stock = fuelStockService.updateFuelStock(stockId, newStock, newPrice);
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/station/{stationId}")
    @Operation(summary = "Create fuel stock", description = "Create a new fuel stock for a station")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<FuelStockDto> createFuelStock(
            @PathVariable @NotNull Long stationId,
            @RequestParam @NotNull FuelStock.FuelType fuelType,
            @RequestParam @NotNull @Min(value = 0, message = "Le stock initial ne peut pas être négatif") Double stockInitial,
            @RequestParam @NotNull @Positive(message = "La capacité maximale doit être positive") Double capaciteMax,
            @RequestParam @NotNull @Positive(message = "Le prix doit être positif") Double prix) {
        log.info("Creating fuel stock for station ID: {}, type: {}, initial stock: {}L", stationId, fuelType, stockInitial);
        FuelStockDto stock = fuelStockService.createFuelStock(stationId, fuelType, stockInitial, capaciteMax, prix);
        return ResponseEntity.ok(stock);
    }

    @DeleteMapping("/{stockId}")
    @Operation(summary = "Delete fuel stock", description = "Delete a fuel stock")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFuelStock(@PathVariable Long stockId) {
        fuelStockService.deleteFuelStock(stockId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{stockId}/toggle")
    @Operation(summary = "Toggle fuel availability", description = "Toggle fuel availability (enable/disable)")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<FuelStockDto> toggleFuelAvailability(@PathVariable Long stockId) {
        FuelStockDto stock = fuelStockService.toggleFuelAvailability(stockId);
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/{stockId}/add")
    @Operation(summary = "Add fuel to stock", description = "Add a quantity of fuel to an existing stock")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<FuelStockDto> addFuel(
            @PathVariable @NotNull Long stockId,
            @RequestParam @NotNull @Positive(message = "La quantité doit être positive") Double quantity) {
        log.info("Adding {}L to stock ID: {}", quantity, stockId);
        FuelStockDto stock = fuelStockService.addFuel(stockId, quantity);
        log.info("Successfully added fuel to stock ID: {}", stockId);
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/{stockId}/remove")
    @Operation(summary = "Remove fuel from stock", description = "Remove a quantity of fuel from an existing stock")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<FuelStockDto> removeFuel(
            @PathVariable @NotNull Long stockId,
            @RequestParam @NotNull @Positive(message = "La quantité doit être positive") Double quantity) {
        log.info("Removing {}L from stock ID: {}", quantity, stockId);
        FuelStockDto stock = fuelStockService.removeFuel(stockId, quantity);
        log.info("Successfully removed fuel from stock ID: {}", stockId);
        return ResponseEntity.ok(stock);
    }
}

