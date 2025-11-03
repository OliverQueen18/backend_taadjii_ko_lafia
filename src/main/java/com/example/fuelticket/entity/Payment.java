package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double montant;
    private LocalDateTime datePaiement;
    private String modePaiement;
    @OneToOne
    private Ticket ticket;
}
