package com.example.fuelticket.dto;

import com.example.fuelticket.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private User.Role role;
    private Boolean emailVerified;
    private String message;
}
