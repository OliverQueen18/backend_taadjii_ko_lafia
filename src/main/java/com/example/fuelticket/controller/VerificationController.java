package com.example.fuelticket.controller;

import com.example.fuelticket.dto.VerificationRequest;
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
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "Email verification APIs")
public class VerificationController {
    
    private final UserService userService;
    
    @PostMapping("/verify")
    @Operation(summary = "Verify email with code", description = "Verify user email with verification code")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        try {
            boolean verified = userService.verifyEmail(request.getEmail(), request.getCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", verified);
            
            if (verified) {
                response.put("message", "Email vérifié avec succès !");
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
    
    @PostMapping("/resend")
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
}
