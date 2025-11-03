package com.example.fuelticket.dto;

import com.example.fuelticket.entity.SaleSchedule;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleScheduleDto {
    private Long id;
    private Long stationId;
    private String stationName;
    private SaleSchedule.FuelType fuelType;
    private String fuelTypeDisplayName;
    private LocalDate saleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double availableQuantity;
    private Double maxQuantityPerTicket;
    private Integer maxTicketsPerDay;
    private Boolean isActive;
    private Double remainingQuantity;
    private Integer remainingTickets;
    
    public static SaleScheduleDto fromEntity(SaleSchedule schedule) {
        SaleScheduleDto dto = SaleScheduleDto.builder()
                .id(schedule.getId())
                .stationId(schedule.getStation() != null ? schedule.getStation().getId() : null)
                .stationName(schedule.getStation() != null ? schedule.getStation().getNom() : null)
                .fuelType(schedule.getFuelType())
                .fuelTypeDisplayName(schedule.getFuelType() != null ? schedule.getFuelType().getDisplayName() : null)
                .saleDate(schedule.getSaleDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .availableQuantity(schedule.getAvailableQuantity())
                .maxQuantityPerTicket(schedule.getMaxQuantityPerTicket())
                .maxTicketsPerDay(schedule.getMaxTicketsPerDay())
                .isActive(schedule.getIsActive())
                .build();
        
        // Calculer remainingQuantity et remainingTickets de manière sécurisée
        try {
            dto.setRemainingQuantity(schedule.getRemainingQuantity());
        } catch (Exception e) {
            // Si une exception se produit (ex: LazyInitializationException), utiliser availableQuantity
            dto.setRemainingQuantity(schedule.getAvailableQuantity());
        }
        
        try {
            dto.setRemainingTickets(schedule.getRemainingTickets());
        } catch (Exception e) {
            // Si une exception se produit (ex: LazyInitializationException), utiliser maxTicketsPerDay
            dto.setRemainingTickets(schedule.getMaxTicketsPerDay());
        }
        
        return dto;
    }
}
