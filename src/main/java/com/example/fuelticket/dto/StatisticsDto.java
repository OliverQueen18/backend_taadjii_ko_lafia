package com.example.fuelticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private long citizensCount;
    private long stationsCount;
    private long availableStationsCount;
}

