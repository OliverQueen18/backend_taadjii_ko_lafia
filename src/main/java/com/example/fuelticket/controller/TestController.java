package com.example.fuelticket.controller;

import com.example.fuelticket.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Test APIs for development")
public class TestController {
    
    private final EmailService emailService;
    
    @PostMapping("/email/verification")
    @Operation(summary = "Test verification email", description = "Send a test verification email to specified address")
    public ResponseEntity<Map<String, Object>> testVerificationEmail(@RequestParam String email, @RequestParam String nom) {
        try {
            String testCode = "123456"; // Code de test fixe
            emailService.sendVerificationEmail(email, nom, testCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email de vérification envoyé avec succès à " + email);
            response.put("testCode", testCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi de l'email : " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/email/welcome")
    @Operation(summary = "Test welcome email", description = "Send a test welcome email to specified address")
    public ResponseEntity<Map<String, Object>> testWelcomeEmail(@RequestParam String email, @RequestParam String nom, @RequestParam(defaultValue = "CITOYEN") String role) {
        try {
            emailService.sendWelcomeEmail(email, nom, role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email de bienvenue envoyé avec succès à " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi de l'email : " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/email/config")
    @Operation(summary = "Get email configuration", description = "Get current email configuration (without sensitive data)")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "smtp.gmail.com");
        config.put("port", 587);
        config.put("username", "oliveservicespro@gmail.com");
        config.put("fromEmail", "noreply@taadjikolafia.ml");
        config.put("fromName", "Taadji Ko Lafia");
        config.put("auth", true);
        config.put("starttls", true);
        
        return ResponseEntity.ok(config);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Test registration", description = "Test registration endpoint without email sending")
    public ResponseEntity<Map<String, Object>> testRegister(@RequestBody Map<String, Object> userData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Simuler l'inscription sans email
            response.put("success", true);
            response.put("message", "Test d'inscription réussi (sans email)");
            response.put("userData", userData);
            response.put("token", null);
            response.put("emailVerified", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors du test d'inscription: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{email}/verification-code")
    @Operation(summary = "Get verification code", description = "Get verification code for a user (for testing)")
    public ResponseEntity<Map<String, Object>> getVerificationCode(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Cette méthode devrait être dans UserService, mais pour le test...
            response.put("success", true);
            response.put("message", "Code de vérification récupéré (pour test uniquement)");
            response.put("email", email);
            response.put("note", "Vérifiez les logs du serveur pour voir le code généré");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
