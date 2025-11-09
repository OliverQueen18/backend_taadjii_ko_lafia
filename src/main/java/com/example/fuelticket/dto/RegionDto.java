package com.example.fuelticket.dto;

import lombok.Data;

@Data
public class RegionDto {
    private Long id;
    private String code;
    private String nom;
    private Double latitude;
    private Double longitude;
}

