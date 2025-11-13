package com.example.fuelticket.controller;

import com.example.fuelticket.dto.StatisticsDto;
import com.example.fuelticket.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistics APIs")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/public")
    @Operation(summary = "Get public statistics", description = "Retrieve public statistics including citizens and stations count (no authentication required)")
    public ResponseEntity<StatisticsDto> getPublicStatistics() {
        StatisticsDto statistics = statisticsService.getPublicStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping
    @Operation(summary = "Get statistics", description = "Retrieve statistics (requires authentication)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GESTIONNAIRE')")
    public ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto statistics = statisticsService.getPublicStatistics();
        return ResponseEntity.ok(statistics);
    }
}

