package com.example.fuelticket.controller;

import com.example.fuelticket.dto.CreateSaleScheduleRequest;
import com.example.fuelticket.dto.SaleScheduleDto;
import com.example.fuelticket.service.SaleScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sale-schedules")
@RequiredArgsConstructor
@Tag(name = "Sale Schedules", description = "Sale schedule management APIs")
public class SaleScheduleController {
    
    private final SaleScheduleService saleScheduleService;
    
    @PostMapping
    @Operation(summary = "Create sale schedule", description = "Create a new sale schedule for a station")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<SaleScheduleDto> createSaleSchedule(@Valid @RequestBody CreateSaleScheduleRequest request) {
        SaleScheduleDto schedule = saleScheduleService.createSaleSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }
    
    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get sale schedules by station", description = "Get all active sale schedules for a station")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<SaleScheduleDto>> getSaleSchedulesByStation(@PathVariable Long stationId) {
        List<SaleScheduleDto> schedules = saleScheduleService.getSaleSchedulesByStation(stationId);
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/station/{stationId}/all")
    @Operation(summary = "Get all sale schedules by station (including inactive)", description = "Get all sale schedules for a station (active and inactive)")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<List<SaleScheduleDto>> getAllSaleSchedulesByStation(@PathVariable Long stationId) {
        List<SaleScheduleDto> schedules = saleScheduleService.getAllSaleSchedulesByStation(stationId);
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all sale schedules", description = "Get all sale schedules from all stations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE') or hasRole('STATION')")
    public ResponseEntity<List<SaleScheduleDto>> getAllSaleSchedules() {
        List<SaleScheduleDto> schedules = saleScheduleService.getAllSaleSchedules();
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/station/{stationId}/date/{date}")
    @Operation(summary = "Get sale schedules by station and date", description = "Get sale schedules for a specific station and date")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<List<SaleScheduleDto>> getSaleSchedulesByStationAndDate(
            @PathVariable Long stationId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SaleScheduleDto> schedules = saleScheduleService.getSaleSchedulesByStationAndDate(stationId, date);
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/station/{stationId}/date-range")
    @Operation(summary = "Get sale schedules by date range", description = "Get sale schedules for a station within a date range")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<List<SaleScheduleDto>> getSaleSchedulesByDateRange(
            @PathVariable Long stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SaleScheduleDto> schedules = saleScheduleService.getSaleSchedulesByStationAndDateRange(stationId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }
    
    @PutMapping("/{scheduleId}")
    @Operation(summary = "Update sale schedule", description = "Update an existing sale schedule")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<SaleScheduleDto> updateSaleSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody CreateSaleScheduleRequest request) {
        SaleScheduleDto schedule = saleScheduleService.updateSaleSchedule(scheduleId, request);
        return ResponseEntity.ok(schedule);
    }
    
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Delete sale schedule", description = "Delete a sale schedule")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSaleSchedule(@PathVariable Long scheduleId) {
        saleScheduleService.deleteSaleSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{scheduleId}/toggle")
    @Operation(summary = "Toggle schedule status", description = "Activate or deactivate a sale schedule")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<SaleScheduleDto> toggleScheduleStatus(@PathVariable Long scheduleId) {
        SaleScheduleDto schedule = saleScheduleService.toggleScheduleStatus(scheduleId);
        return ResponseEntity.ok(schedule);
    }
    
    @GetMapping("/available/{stationId}")
    @Operation(summary = "Get available schedules for ticket", description = "Get available schedules for creating a ticket")
    public ResponseEntity<List<SaleScheduleDto>> getAvailableSchedulesForTicket(
            @PathVariable Long stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String fuelType) {
        List<SaleScheduleDto> schedules = saleScheduleService.getAvailableSchedulesForTicket(
                stationId, date, com.example.fuelticket.entity.SaleSchedule.FuelType.valueOf(fuelType));
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/planned-dates/{stationId}")
    @Operation(summary = "Get planned dates for station", description = "Get all planned dates (>= today) for a station")
    @PreAuthorize("hasRole('CITOYEN') or hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<List<LocalDate>> getPlannedDatesForStation(@PathVariable Long stationId) {
        List<LocalDate> dates = saleScheduleService.getPlannedDatesForStation(stationId);
        return ResponseEntity.ok(dates);
    }
}
