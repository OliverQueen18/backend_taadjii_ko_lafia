package com.example.fuelticket.dto;

import lombok.Data;

@Data
public class RepartitionDto {
    private Long id;
    private Long corpsId;
    private String corpsNom;
    private Long stationId;
    private String stationNom;
    private String commentaire;
}

