package com.example.fuelticket.service;

import com.example.fuelticket.entity.Ticket;
import com.example.fuelticket.entity.SaleSchedule;
import com.example.fuelticket.repository.TicketRepository;
import com.example.fuelticket.repository.SaleScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketExpirationService {
    
    private final TicketRepository ticketRepository;
    private final SaleScheduleRepository saleScheduleRepository;
    private final PdfGenerationService pdfGenerationService;
    
    // Exécuter tous les jours à 00:00
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processExpiredTickets() {
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
        
        // Trouver tous les tickets non servis de plus de 5 jours
        List<Ticket> expiredTickets = ticketRepository.findByStatutAndDateCreationBefore(
            Ticket.Statut.EN_ATTENTE, 
            fiveDaysAgo.atStartOfDay()
        );
        
        for (Ticket ticket : expiredTickets) {
            renewTicket(ticket);
        }
    }
    
    @Transactional
    public void renewTicket(Ticket oldTicket) {
        // Marquer l'ancien ticket comme expiré
        oldTicket.markAsExpired();
        ticketRepository.save(oldTicket);
        
        // Trouver une nouvelle planification disponible
        SaleSchedule newSchedule = findAvailableScheduleForRenewal(oldTicket);
        if (newSchedule == null) {
            // Si aucune planification disponible, créer un ticket avec une date future
            createTicketWithFutureDate(oldTicket);
            return;
        }
        
        // Créer un nouveau ticket
        Ticket newTicket = Ticket.builder()
                .numeroTicket(generateTicketNumber())
                .numeroOrdre(generateOrderNumber())
                .dateApprovisionnement(newSchedule.getSaleDate())
                .typeCarburant(oldTicket.getTypeCarburant())
                .quantite(oldTicket.getQuantite())
                .statut(Ticket.Statut.EN_ATTENTE)
                .citoyen(oldTicket.getCitoyen())
                .station(oldTicket.getStation())
                .saleSchedule(newSchedule)
                .emailCitoyen(oldTicket.getEmailCitoyen())
                .nomCitoyen(oldTicket.getNomCitoyen())
                .prenomCitoyen(oldTicket.getPrenomCitoyen())
                .telephoneCitoyen(oldTicket.getTelephoneCitoyen())
                .montantPaye(oldTicket.getMontantPaye())
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(5)) // 5 jours pour le nouveau ticket
                .dateDerniereMiseAJour(LocalDateTime.now())
                .ancienNumeroTicket(oldTicket.getNumeroTicket())
                .build();
        
        // Générer le QR code et le PDF
        try {
            newTicket.setQrCodeData(pdfGenerationService.generateQRCodeData(newTicket));
            String pdfPath = pdfGenerationService.generateTicketPdf(newTicket);
            newTicket.setPdfPath(pdfPath);
        } catch (Exception e) {
            // Log l'erreur mais continue la création du ticket
            System.err.println("Erreur lors de la génération du PDF pour le ticket " + newTicket.getNumeroTicket() + ": " + e.getMessage());
        }
        
        ticketRepository.save(newTicket);
        
        // Mettre à jour la quantité disponible dans la planification
        updateScheduleQuantity(newSchedule, newTicket.getQuantite());
    }
    
    private SaleSchedule findAvailableScheduleForRenewal(Ticket oldTicket) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        
        List<SaleSchedule> availableSchedules = saleScheduleRepository.findActiveSchedulesByStationAndDateRange(
            oldTicket.getStation(), 
            tomorrow, 
            nextWeek
        );
        
        return availableSchedules.stream()
                .filter(schedule -> schedule.getFuelType().name().equals(oldTicket.getTypeCarburant()))
                .filter(SaleSchedule::isAvailableForSale)
                .filter(schedule -> schedule.getRemainingQuantity() >= oldTicket.getQuantite())
                .filter(schedule -> schedule.getRemainingTickets() > 0)
                .findFirst()
                .orElse(null);
    }
    
    private void createTicketWithFutureDate(Ticket oldTicket) {
        // Créer un ticket avec une date future (dans 7 jours)
        LocalDate futureDate = LocalDate.now().plusDays(7);
        
        Ticket newTicket = Ticket.builder()
                .numeroTicket(generateTicketNumber())
                .numeroOrdre(generateOrderNumber())
                .dateApprovisionnement(futureDate)
                .typeCarburant(oldTicket.getTypeCarburant())
                .quantite(oldTicket.getQuantite())
                .statut(Ticket.Statut.EN_ATTENTE)
                .citoyen(oldTicket.getCitoyen())
                .station(oldTicket.getStation())
                .emailCitoyen(oldTicket.getEmailCitoyen())
                .nomCitoyen(oldTicket.getNomCitoyen())
                .prenomCitoyen(oldTicket.getPrenomCitoyen())
                .telephoneCitoyen(oldTicket.getTelephoneCitoyen())
                .montantPaye(oldTicket.getMontantPaye())
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(5))
                .dateDerniereMiseAJour(LocalDateTime.now())
                .ancienNumeroTicket(oldTicket.getNumeroTicket())
                .build();
        
        ticketRepository.save(newTicket);
    }
    
    private void updateScheduleQuantity(SaleSchedule schedule, Double usedQuantity) {
        schedule.setAvailableQuantity(schedule.getAvailableQuantity() - usedQuantity);
        saleScheduleRepository.save(schedule);
    }
    
    private String generateTicketNumber() {
        return "TK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
    
    private String generateOrderNumber() {
        return String.format("%03d", (int) (Math.random() * 1000));
    }
    
    @Transactional
    public void markExpiredTickets() {
        List<Ticket> tickets = ticketRepository.findByStatut(Ticket.Statut.EN_ATTENTE);
        
        for (Ticket ticket : tickets) {
            if (ticket.isExpired()) {
                ticket.markAsExpired();
                ticketRepository.save(ticket);
            }
        }
    }
}
