package com.example.fuelticket.controller;

import com.example.fuelticket.dto.AddUserToStationRequest;
import com.example.fuelticket.dto.UserDto;
import com.example.fuelticket.service.UserService;
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
@RequestMapping("/api/stations/{stationId}/users")
@RequiredArgsConstructor
@Tag(name = "Station Users", description = "Station user management APIs")
public class StationUserController {
    
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "Get users by station", description = "Get all users associated with a station")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUsersByStation(@PathVariable Long stationId) {
        List<UserDto> users = userService.getUsersByStation(stationId);
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    @Operation(summary = "Add user to station", description = "Add a new user to a station")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> addUserToStation(@PathVariable Long stationId, 
                                                   @Valid @RequestBody AddUserToStationRequest request) {
        UserDto user = userService.addUserToStation(stationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
