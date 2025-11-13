package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.example.fuelticket.entity.SaleSchedule")
public class SaleSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;
    
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;
    
    private LocalDate saleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double availableQuantity; // Quantité disponible pour la vente
    private Double maxQuantityPerTicket; // Quantité maximale par ticket
    private Integer maxTicketsPerDay; // Nombre maximum de tickets par jour
    private Boolean isActive = true; // Si la planification est active
    
    @OneToMany(mappedBy = "saleSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;
    
    public enum FuelType {
        ESSENCE("Essence"),
        DIESEL("Diesel"),
        GPL("GPL"),
        KEROSENE("Kérosène");
        
        private final String displayName;
        
        FuelType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Méthodes utilitaires
    public boolean isDateInRange(LocalDate date) {
        return saleDate.equals(date);
    }
    
    public boolean isTimeInRange(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    public boolean isAvailableForSale() {
        return isActive && availableQuantity > 0;
    }
    
    public Double getRemainingQuantity() {
        if (tickets == null) return availableQuantity;
        
        double usedQuantity = tickets.stream()
            .filter(ticket -> ticket.getStatut() != Ticket.Statut.ANNULE)
            .mapToDouble(Ticket::getQuantite)
            .sum();
            
        return Math.max(0, availableQuantity - usedQuantity);
    }
    
    public Integer getRemainingTickets() {
        if (tickets == null) return maxTicketsPerDay;
        
        long usedTickets = tickets.stream()
            .filter(ticket -> ticket.getStatut() != Ticket.Statut.ANNULE)
            .count();
            
        return Math.max(0, maxTicketsPerDay - (int) usedTickets);
    }
}
