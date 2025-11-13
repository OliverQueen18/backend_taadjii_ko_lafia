package com.example.fuelticket.controller;

import com.example.fuelticket.service.StatisticsService;
import com.example.fuelticket.service.TicketService;
import com.example.fuelticket.service.StationService;
import com.example.fuelticket.service.UserService;
import com.example.fuelticket.dto.StatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reports APIs for Gestionnaires")
public class ReportController {

    private final StatisticsService statisticsService;
    private final TicketService ticketService;
    private final StationService stationService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get reports data", description = "Retrieve comprehensive reports data for gestionnaires")
    @PreAuthorize("hasRole('GESTIONNAIRE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReports() {
        Map<String, Object> reports = new HashMap<>();
        
        // Statistiques générales
        StatisticsDto statistics = statisticsService.getPublicStatistics();
        reports.put("statistics", statistics);
        
        // Nombre total de tickets
        long totalTickets = ticketService.getAllTickets().size();
        reports.put("totalTickets", totalTickets);
        
        // Nombre total de stations
        long totalStations = stationService.getAllStations().size();
        reports.put("totalStations", totalStations);
        
        // Nombre total d'utilisateurs
        long totalUsers = userService.getAllUsers().size();
        reports.put("totalUsers", totalUsers);
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get reports summary", description = "Retrieve a summary of reports data")
    @PreAuthorize("hasRole('GESTIONNAIRE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReportsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        StatisticsDto statistics = statisticsService.getPublicStatistics();
        summary.put("statistics", statistics);
        
        return ResponseEntity.ok(summary);
    }
}

