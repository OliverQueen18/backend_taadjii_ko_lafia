package com.example.fuelticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddUserToStationRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\+223[0-9]{8}$", message = "Format: +223XXXXXXXX")
    private String telephone;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
