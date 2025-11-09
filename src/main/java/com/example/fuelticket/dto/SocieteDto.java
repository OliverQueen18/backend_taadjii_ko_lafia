package com.example.fuelticket.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SocieteDto {
    private Long id;
    private String nom;
    private String description;
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

