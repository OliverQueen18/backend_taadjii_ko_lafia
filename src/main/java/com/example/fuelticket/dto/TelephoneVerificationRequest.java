package com.example.fuelticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TelephoneVerificationRequest {
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\+223[0-9]{8}$", message = "Format: +223XXXXXXXX")
    private String telephone;
    
    @NotBlank(message = "Le code de vérification est obligatoire")
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code doit contenir exactement 6 chiffres")
    private String code;
}

