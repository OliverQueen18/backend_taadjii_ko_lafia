package com.example.fuelticket.controller;

import com.example.fuelticket.dto.AuthRequest;
import com.example.fuelticket.dto.AuthResponse;
import com.example.fuelticket.dto.UserDto;
import com.example.fuelticket.dto.VerificationRequest;
import com.example.fuelticket.dto.TelephoneVerificationRequest;
import com.example.fuelticket.service.AuthService;
import com.example.fuelticket.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final com.example.fuelticket.repository.StationRepository stationRepository;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user and send verification email")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserDto userDto) {
        AuthResponse response = authService.register(userDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email with code", description = "Verify user email with verification code and return JWT token")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        try {
            boolean verified = userService.verifyEmail(request.getEmail(), request.getCode());
            
            if (verified) {
                // Générer un token JWT après vérification réussie
                AuthResponse response = authService.verifyEmailAndLogin(request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                // Retourner une réponse d'erreur
                AuthResponse errorResponse = AuthResponse.builder()
                        .token(null)
                        .emailVerified(false)
                        .message("Code de vérification invalide ou expiré")
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .token(null)
                    .emailVerified(false)
                    .message("Erreur lors de la vérification : " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification code", description = "Resend verification code to user email")
    public ResponseEntity<Map<String, Object>> resendVerificationCode(@RequestParam String email) {
        try {
            userService.resendVerificationCode(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Code de vérification renvoyé avec succès !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors du renvoi du code : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset code by email")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "L'email est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            authService.forgotPassword(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Un code de réinitialisation a été envoyé à votre adresse email");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de l'envoi du code de réinitialisation");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/verify-telephone")
    @Operation(summary = "Verify telephone with code", description = "Verify user telephone with verification code sent by SMS")
    public ResponseEntity<Map<String, Object>> verifyTelephone(@Valid @RequestBody TelephoneVerificationRequest request) {
        try {
            boolean verified = userService.verifyTelephone(request.getTelephone(), request.getCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", verified);
            
            if (verified) {
                response.put("message", "Téléphone vérifié avec succès !");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Code de vérification invalide ou expiré");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("verified", false);
            response.put("message", "Erreur lors de la vérification : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/resend-telephone-verification")
    @Operation(summary = "Resend telephone verification code", description = "Resend verification code to user telephone by SMS")
    public ResponseEntity<Map<String, Object>> resendTelephoneVerificationCode(@RequestParam String telephone) {
        try {
            userService.resendTelephoneVerificationCode(telephone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Code de vérification renvoyé avec succès par SMS !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors du renvoi du code : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<UserDto> getCurrentUser() {
        var user = authService.getCurrentUser();
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setNom(user.getNom());
        userDto.setPrenom(user.getPrenom());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        // Compat: transmettre une stationId (première station gérée) si disponible
        var stations = stationRepository.findByManagerId(user.getId());
        if (!stations.isEmpty()) {
            userDto.setStationId(stations.get(0).getId());
        }
        return ResponseEntity.ok(userDto);
    }
}
