package com.example.fuelticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerificationRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le code de vérification est obligatoire")
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code doit contenir exactement 6 chiffres")
    private String code;
}
