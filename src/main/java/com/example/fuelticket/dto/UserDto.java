package com.example.fuelticket.dto;

import com.example.fuelticket.entity.User;
import com.example.fuelticket.validation.OptionalPattern;
import com.example.fuelticket.validation.OptionalSize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.example.fuelticket.dto.StationDto;

@Data
public class UserDto {
    private Long id;

    @NotBlank(message = "Nom is required")
    @Size(min = 2, max = 50, message = "Nom must be between 2 and 50 characters")
    private String nom;

    @NotBlank(message = "Prenom is required")
    @Size(min = 2, max = 50, message = "Prenom must be between 2 and 50 characters")
    private String prenom;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Password est optionnel pour la mise à jour, mais requis pour la création
    // La validation est gérée dans les services (createUser, register)
    // @OptionalSize valide seulement si password est fourni (non null et non vide)
    @OptionalSize(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Pattern optionnel : validé seulement si fourni (non null et non vide)
    // La validation obligatoire pour CITOYEN est gérée dans AuthService
    @OptionalPattern(regexp = "^\\+223[0-9]{8}$", message = "Format: +223XXXXXXXX")
    private String telephone;

    @NotNull(message = "Role is required")
    private User.Role role;
    
    // Champs pour la validation
    private Boolean emailVerified = false;
    private Long stationId; // legacy: peut être ignoré si station est fourni

    // Pour l'inscription d'un gérant: création automatique de station
    private StationDto station;
}
