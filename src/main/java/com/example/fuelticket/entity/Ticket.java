package com.example.fuelticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String numeroTicket; // ex UUID short
    private String numeroOrdre; // ex 001
    private LocalDate dateApprovisionnement;
    private String typeCarburant;
    private Double quantite;
    @Enumerated(EnumType.STRING)
    private Statut statut;
    @ManyToOne
    private User citoyen;
    @ManyToOne
    private Station station;
    
    // Relation avec la planification de vente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_schedule_id")
    private SaleSchedule saleSchedule;
    
    // Nouveaux champs pour les tickets payés en espèces
    private String emailCitoyen; // Email du citoyen pour les tickets payés en espèces
    private String nomCitoyen; // Nom du citoyen
    private String prenomCitoyen; // Prénom du citoyen
    private String telephoneCitoyen; // Téléphone du citoyen
    private Double montantPaye; // Montant payé en espèces
    private LocalDateTime dateCreation; // Date de création du ticket
    private String qrCodeData; // Données du QR code
    private String pdfPath; // Chemin vers le PDF généré
    
    // Gestion des dates et expiration
    private LocalDateTime dateExpiration; // Date d'expiration du ticket
    private LocalDateTime dateDerniereMiseAJour; // Date de dernière mise à jour
    private Boolean isExpired = false; // Si le ticket est expiré
    private String ancienNumeroTicket; // Ancien numéro si le ticket a été renouvelé

    public enum Statut { EN_ATTENTE, VALIDE, SERVI, ANNULE, EXPIRE }
    
    // Méthodes utilitaires
    public boolean isExpired() {
        if (isExpired) return true;
        return dateExpiration != null && LocalDateTime.now().isAfter(dateExpiration);
    }
    
    public boolean needsRenewal() {
        return statut == Statut.EN_ATTENTE && isExpired();
    }
    
    public void markAsExpired() {
        this.isExpired = true;
        this.statut = Statut.EXPIRE;
        this.dateDerniereMiseAJour = LocalDateTime.now();
    }
}
