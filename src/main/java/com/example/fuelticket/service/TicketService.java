package com.example.fuelticket.service;

import com.example.fuelticket.dto.TicketDto;
import com.example.fuelticket.dto.CashTicketRequest;
import com.example.fuelticket.dto.SaleScheduleDto;
import com.example.fuelticket.entity.*;
import com.example.fuelticket.repository.*;
import com.example.fuelticket.service.SaleScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final SaleScheduleRepository saleScheduleRepository;
    private final PdfGenerationService pdfGenerationService;
    private final SaleScheduleService saleScheduleService;
    private final EmailService emailService;
    private final AuthService authService;

    @Transactional
    public TicketDto creerTicket(Long userId, Long stationId, String typeCarburant, Double quantite, LocalDate dateApprovisionnement) {
        User u = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Charger la station avec son manager pour éviter LazyInitializationException
        Station s = stationRepository.findByIdWithManager(stationId)
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));

        // Vérifier s'il y a des tickets en attente pour cet utilisateur
        List<Ticket.Statut> statutsEnAttente = Arrays.asList(Ticket.Statut.EN_ATTENTE, Ticket.Statut.VALIDE);
        if (ticketRepository.existsByCitoyenAndStatutIn(u, statutsEnAttente)) {
            throw new RuntimeException("Vous avez déjà un ticket en attente ou validé. Veuillez attendre qu'il soit servi avant d'en créer un nouveau.");
        }

        // Utiliser la date fournie ou aujourd'hui par défaut
        LocalDate targetDate = dateApprovisionnement != null ? dateApprovisionnement : LocalDate.now();
        
        // Vérifier que la date n'est pas dans le passé
        if (targetDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("La date d'approvisionnement ne peut pas être dans le passé.");
        }
        
        SaleSchedule.FuelType fuelType = SaleSchedule.FuelType.valueOf(typeCarburant.toUpperCase());
        
        List<SaleScheduleDto> availableSchedules = saleScheduleService.getAvailableSchedulesForTicket(stationId, targetDate, fuelType);
        
        if (availableSchedules.isEmpty()) {
            throw new RuntimeException("Aucune planification de vente disponible pour ce type de carburant à la date sélectionnée. Veuillez contacter la station.");
        }
        
        // Prendre la première planification disponible
        SaleScheduleDto selectedSchedule = availableSchedules.get(0);
        
        // Vérifier que la quantité demandée ne dépasse pas la quantité maximale par ticket
        if (quantite > selectedSchedule.getMaxQuantityPerTicket()) {
            throw new RuntimeException("La quantité demandée (" + quantite + "L) dépasse la quantité maximale autorisée par ticket (" + selectedSchedule.getMaxQuantityPerTicket() + "L)");
        }
        
        // Vérifier qu'il reste de la place pour un nouveau ticket
        if (selectedSchedule.getRemainingTickets() <= 0) {
            throw new RuntimeException("Aucun ticket disponible pour cette date. Tous les tickets ont été attribués.");
        }
        
        // Vérifier qu'il reste assez de quantité
        if (selectedSchedule.getRemainingQuantity() < quantite) {
            throw new RuntimeException("Quantité insuffisante. Quantité restante: " + selectedSchedule.getRemainingQuantity() + "L");
        }

        // Calcul du nombre de tickets déjà planifiés pour la date cible
        long ordre = ticketRepository.countByStationAndDateApprovisionnement(s, targetDate) + 1;

        // Génération du numéro de ticket unique
        String numeroTicket = generateUniqueTicketNumber();
        String numeroOrdre = String.format("%03d", ordre);

        // Récupérer la planification complète
        SaleSchedule schedule = saleScheduleRepository.findById(selectedSchedule.getId())
                .orElseThrow(() -> new RuntimeException("Planification non trouvée"));

        // Remplir les informations du citoyen dans le ticket
        Ticket t = Ticket.builder()
                .numeroTicket(numeroTicket)
                .numeroOrdre(numeroOrdre)
                .dateApprovisionnement(targetDate)
                .typeCarburant(typeCarburant)
                .quantite(quantite)
                .statut(Ticket.Statut.EN_ATTENTE)
                .citoyen(u)
                .station(s)
                .saleSchedule(schedule)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(5)) // 5 jours d'expiration
                .dateDerniereMiseAJour(LocalDateTime.now())
                .isExpired(false) // Initialiser à false par défaut
                // Remplir les champs du citoyen
                .emailCitoyen(u.getEmail())
                .nomCitoyen(u.getNom())
                .prenomCitoyen(u.getPrenom())
                .telephoneCitoyen(u.getTelephone())
                .build();

        Ticket savedTicket = ticketRepository.save(t);
        
        // Mettre à jour la quantité disponible dans la planification
        schedule.setAvailableQuantity(schedule.getAvailableQuantity() - quantite);
        saleScheduleRepository.save(schedule);

        // Envoyer un email au gérant de station pour l'informer du nouveau ticket
        try {
            if (s.getManager() != null && s.getManager().getEmail() != null) {
                emailService.sendNewTicketNotificationToManager(
                    s.getManager().getEmail(),
                    s.getManager().getNom(),
                    s.getNom(),
                    savedTicket,
                    u
                );
            }
        } catch (Exception e) {
            // Ne pas faire échouer la création du ticket si l'envoi d'email échoue
            System.err.println("⚠️ Erreur lors de l'envoi de l'email au gérant de station : " + e.getMessage());
        }

        return convertToDto(savedTicket);
    }

    @Transactional
    public TicketDto creerTicketEspeces(CashTicketRequest request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new RuntimeException("Station non trouvée"));

        // Vérifier s'il y a des tickets en attente pour cet email de citoyen
        List<Ticket.Statut> statutsEnAttente = Arrays.asList(Ticket.Statut.EN_ATTENTE, Ticket.Statut.VALIDE);
        if (ticketRepository.existsByEmailCitoyenAndStatutIn(request.getEmailCitoyen(), statutsEnAttente)) {
            throw new RuntimeException("Il existe déjà un ticket en attente ou validé pour cet email. Veuillez attendre qu'il soit servi avant d'en créer un nouveau.");
        }

        // Vérifier le stock disponible
        if (station.getStockTotalDisponible() < request.getQuantite()) {
            throw new RuntimeException("Stock insuffisant. Stock disponible: " + station.getStockTotalDisponible() + " litres");
        }

        // Calcul du nombre de tickets déjà planifiés pour la date demandée
        long ordre = ticketRepository.countByStationAndDateApprovisionnement(station, request.getDateApprovisionnement()) + 1;

        // Génération du numéro de ticket unique
        String numeroTicket = generateUniqueTicketNumber();
        String numeroOrdre = String.format("%03d", ordre);

        // Créer le ticket
        Ticket ticket = Ticket.builder()
                .numeroTicket(numeroTicket)
                .numeroOrdre(numeroOrdre)
                .dateApprovisionnement(request.getDateApprovisionnement())
                .typeCarburant(request.getTypeCarburant())
                .quantite(request.getQuantite())
                .statut(Ticket.Statut.VALIDE) // Directement valide pour les paiements en espèces
                .station(station)
                .emailCitoyen(request.getEmailCitoyen())
                .nomCitoyen(request.getNomCitoyen())
                .prenomCitoyen(request.getPrenomCitoyen())
                .telephoneCitoyen(request.getTelephoneCitoyen())
                .montantPaye(request.getMontantPaye())
                .dateCreation(LocalDateTime.now())
                .build();

        // Générer les données du QR code
        String qrCodeData = pdfGenerationService.generateQRCodeData(ticket);
        ticket.setQrCodeData(qrCodeData);

        // Sauvegarder le ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Note: Stock is managed through FuelStock entities, not directly on Station
        // The stock update should be handled through the FuelStockService

        // Générer le PDF
        try {
            String pdfPath = pdfGenerationService.generateTicketPdf(savedTicket);
            savedTicket.setPdfPath(pdfPath);
            ticketRepository.save(savedTicket);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la création du ticket
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
        }

        return convertToDto(savedTicket);
    }

    public List<TicketDto> getAllTickets() {
        return ticketRepository.findAllWithCitoyenAndStation().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TicketDto getTicketById(Long id) {
        Ticket ticket = ticketRepository.findByIdWithCitoyenAndStation(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        return convertToDto(ticket);
    }

    public TicketDto getTicketByNumeroTicket(String numeroTicket) {
        Ticket ticket = ticketRepository.findByNumeroTicketWithCitoyenAndStation(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket not found with number: " + numeroTicket));
        return convertToDto(ticket);
    }

    public List<TicketDto> getTicketsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return ticketRepository.findByCitoyenWithStation(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TicketDto> getTicketsByStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + stationId));
        return ticketRepository.findByStationWithCitoyen(station).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketDto updateTicketStatus(Long id, Ticket.Statut newStatus, Long userId) {
        Ticket ticket = ticketRepository.findByIdWithCitoyenAndStation(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Vérifier si c'est un citoyen qui essaie de modifier le statut
        if (user.getRole() == User.Role.CITOYEN) {
            // Les citoyens ne peuvent que annuler leurs propres tickets
            if (newStatus != Ticket.Statut.ANNULE) {
                throw new RuntimeException("Les citoyens ne peuvent qu'annuler leurs tickets");
            }
            
            // Vérifier que le ticket appartient au citoyen
            if (ticket.getCitoyen() == null || !ticket.getCitoyen().getId().equals(userId)) {
                throw new RuntimeException("Vous ne pouvez annuler que vos propres tickets");
            }
            
            // Les citoyens ne peuvent annuler que les tickets EN_ATTENTE
            if (ticket.getStatut() != Ticket.Statut.EN_ATTENTE) {
                throw new RuntimeException("Vous ne pouvez annuler que les tickets en attente");
            }
        }
        
        // Vérifier les transitions de statut valides
        if (!isValidStatusTransition(ticket.getStatut(), newStatus)) {
            throw new RuntimeException("Transition de statut invalide de " + ticket.getStatut() + " vers " + newStatus);
        }
        
        Ticket.Statut oldStatus = ticket.getStatut();
        ticket.setStatut(newStatus);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Si le statut passe à VALIDE et que c'est un gérant de station qui valide, envoyer le reçu PDF par email
        if (oldStatus == Ticket.Statut.EN_ATTENTE && newStatus == Ticket.Statut.VALIDE && 
            (user.getRole() == User.Role.STATION || user.getRole() == User.Role.ADMIN)) {
            // S'assurer que le QR code est généré
            if (savedTicket.getQrCodeData() == null || savedTicket.getQrCodeData().isEmpty()) {
                String qrCodeData = pdfGenerationService.generateQRCodeData(savedTicket);
                savedTicket.setQrCodeData(qrCodeData);
                savedTicket = ticketRepository.save(savedTicket);
            }
            
            // Générer le PDF
            try {
                String pdfPath = pdfGenerationService.generateTicketPdf(savedTicket);
                savedTicket.setPdfPath(pdfPath);
                savedTicket = ticketRepository.save(savedTicket);
                
                // Envoyer l'email avec le lien de téléchargement du PDF au citoyen
                String citoyenEmail = savedTicket.getEmailCitoyen();
                if (citoyenEmail != null && !citoyenEmail.isEmpty()) {
                    User citoyen = savedTicket.getCitoyen();
                    String citoyenNom = (citoyen != null && citoyen.getNom() != null) ? citoyen.getNom() : 
                                       (savedTicket.getNomCitoyen() != null ? savedTicket.getNomCitoyen() : "Cher client");
                    String citoyenPrenom = (citoyen != null && citoyen.getPrenom() != null) ? citoyen.getPrenom() : 
                                          (savedTicket.getPrenomCitoyen() != null ? savedTicket.getPrenomCitoyen() : "");
                    String nomComplet = citoyenPrenom + " " + citoyenNom;
                    
                    // Construire l'URL de téléchargement
                    String downloadUrl = "/api/tickets/" + savedTicket.getId() + "/pdf";
                    emailService.sendTicketReceiptToCitizen(citoyenEmail, nomComplet, savedTicket, downloadUrl);
                }
            } catch (Exception e) {
                // Ne pas faire échouer la validation du ticket si l'envoi d'email échoue
                System.err.println("⚠️ Erreur lors de la génération/envoi du reçu PDF pour le ticket " + savedTicket.getNumeroTicket() + " : " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return convertToDto(savedTicket);
    }

    public List<TicketDto> getTicketsEnAttenteByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        List<Ticket.Statut> statutsEnAttente = Arrays.asList(Ticket.Statut.EN_ATTENTE, Ticket.Statut.VALIDE);
        return ticketRepository.findByCitoyenAndStatutIn(user, statutsEnAttente).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TicketDto> getTicketsEnAttenteByEmail(String emailCitoyen) {
        List<Ticket.Statut> statutsEnAttente = Arrays.asList(Ticket.Statut.EN_ATTENTE, Ticket.Statut.VALIDE);
        return ticketRepository.findByEmailCitoyenAndStatutIn(emailCitoyen, statutsEnAttente).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean hasTicketEnAttente(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        List<Ticket.Statut> statutsEnAttente = Arrays.asList(Ticket.Statut.EN_ATTENTE, Ticket.Statut.VALIDE);
        return ticketRepository.existsByCitoyenAndStatutIn(user, statutsEnAttente);
    }

    public boolean hasTicketEnAttenteByEmail(String emailCitoyen) {
        List<Ticket.Statut> statutsEnAttente = Arrays.asList(Ticket.Statut.EN_ATTENTE, Ticket.Statut.VALIDE);
        return ticketRepository.existsByEmailCitoyenAndStatutIn(emailCitoyen, statutsEnAttente);
    }

    @Transactional
    public void deleteTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByIdWithCitoyenAndStation(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est soit ADMIN, soit le propriétaire du ticket
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = ticket.getCitoyen() != null && ticket.getCitoyen().getId().equals(userId);
        
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Vous n'avez pas la permission de supprimer ce ticket");
        }
        
        // Un citoyen ne peut supprimer que les tickets EN_ATTENTE (non validés par le gérant)
        if (!isAdmin && ticket.getStatut() != Ticket.Statut.EN_ATTENTE) {
            throw new RuntimeException("Vous ne pouvez supprimer que les tickets en attente. Ce ticket a déjà été traité par la station.");
        }
        
        // Si le ticket est lié à une planification, remettre la quantité dans le stock disponible
        if (ticket.getSaleSchedule() != null && ticket.getSaleSchedule().getAvailableQuantity() != null) {
            SaleSchedule schedule = ticket.getSaleSchedule();
            schedule.setAvailableQuantity(schedule.getAvailableQuantity() + ticket.getQuantite());
            saleScheduleRepository.save(schedule);
        }
        
        ticketRepository.delete(ticket);
    }

    private boolean isValidStatusTransition(Ticket.Statut currentStatus, Ticket.Statut newStatus) {
        // Définir les transitions de statut valides
        switch (currentStatus) {
            case EN_ATTENTE:
                return newStatus == Ticket.Statut.VALIDE || newStatus == Ticket.Statut.ANNULE;
            case VALIDE:
                return newStatus == Ticket.Statut.SERVI || newStatus == Ticket.Statut.ANNULE;
            case SERVI:
                return false; // Un ticket servi ne peut plus changer de statut
            case ANNULE:
                return false; // Un ticket annulé ne peut plus changer de statut
            default:
                return false;
        }
    }

    private String generateUniqueTicketNumber() {
        String numeroTicket;
        do {
            numeroTicket = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (ticketRepository.existsByNumeroTicket(numeroTicket));
        return numeroTicket;
    }

    private LocalDate calculateApprovisionnementDate(Station station, LocalDate today, long ordre, Double quantite) {
        double capacityLitres = station.getCapaciteJournaliere() == null ? 5000.0 : station.getCapaciteJournaliere();
        
        // Calculer le total des litres déjà planifiés pour aujourd'hui
        double totalPlannedLitresToday = ticketRepository.findByStationAndDateApprovisionnement(station, today)
                .stream()
                .mapToDouble(Ticket::getQuantite)
                .sum();
        
        // Si on peut ajouter cette quantité aujourd'hui
        if (totalPlannedLitresToday + quantite <= capacityLitres) {
            return today;
        } else {
            // Calculer combien de jours il faut attendre
            double remainingCapacity = capacityLitres - totalPlannedLitresToday;
            if (remainingCapacity > 0) {
                return today.plusDays(1);
            } else {
                return today.plusDays((long) Math.ceil(quantite / capacityLitres));
            }
        }
    }

    private TicketDto convertToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setNumeroTicket(ticket.getNumeroTicket());
        dto.setNumeroOrdre(ticket.getNumeroOrdre());
        dto.setDateApprovisionnement(ticket.getDateApprovisionnement());
        dto.setTypeCarburant(ticket.getTypeCarburant());
        dto.setQuantite(ticket.getQuantite());
        dto.setStatut(ticket.getStatut());
        
        // Gérer les cas où citoyen ou station peuvent être null
        if (ticket.getCitoyen() != null) {
            dto.setCitoyenId(ticket.getCitoyen().getId());
        }
        
        if (ticket.getStation() != null) {
            dto.setStationId(ticket.getStation().getId());
        }
        
        // Remplir les informations du citoyen (depuis les champs du ticket ou l'entité citoyen)
        if (ticket.getEmailCitoyen() != null) {
            dto.setEmailCitoyen(ticket.getEmailCitoyen());
        } else if (ticket.getCitoyen() != null && ticket.getCitoyen().getEmail() != null) {
            dto.setEmailCitoyen(ticket.getCitoyen().getEmail());
        }
        
        if (ticket.getNomCitoyen() != null) {
            dto.setNomCitoyen(ticket.getNomCitoyen());
        } else if (ticket.getCitoyen() != null && ticket.getCitoyen().getNom() != null) {
            dto.setNomCitoyen(ticket.getCitoyen().getNom());
        }
        
        if (ticket.getPrenomCitoyen() != null) {
            dto.setPrenomCitoyen(ticket.getPrenomCitoyen());
        } else if (ticket.getCitoyen() != null && ticket.getCitoyen().getPrenom() != null) {
            dto.setPrenomCitoyen(ticket.getCitoyen().getPrenom());
        }
        
        if (ticket.getTelephoneCitoyen() != null) {
            dto.setTelephoneCitoyen(ticket.getTelephoneCitoyen());
        } else if (ticket.getCitoyen() != null && ticket.getCitoyen().getTelephone() != null) {
            dto.setTelephoneCitoyen(ticket.getCitoyen().getTelephone());
        }
        
        // Remplir les dates
        dto.setDateCreation(ticket.getDateCreation());
        dto.setDateExpiration(ticket.getDateExpiration());
        
        // Remplir le QR code
        dto.setQrCodeData(ticket.getQrCodeData());
        
        return dto;
    }

    /**
     * Télécharge le PDF d'un ticket
     */
    public ResponseEntity<Resource> downloadTicketPdf(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithCitoyenAndStation(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé avec l'ID: " + ticketId));
        
        // Vérifier les permissions
        User currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        boolean isOwner = ticket.getCitoyen() != null && ticket.getCitoyen().getId().equals(currentUser.getId());
        boolean isStationManager = currentUser.getRole() == User.Role.STATION && 
                                   ticket.getStation() != null && 
                                   ticket.getStation().getManager() != null &&
                                   ticket.getStation().getManager().getId().equals(currentUser.getId());
        
        if (!isAdmin && !isOwner && !isStationManager) {
            throw new RuntimeException("Vous n'avez pas la permission d'accéder à ce ticket");
        }
        
        // Toujours régénérer le PDF pour s'assurer qu'il est à jour
        try {
            // S'assurer que le QR code est généré
            if (ticket.getQrCodeData() == null || ticket.getQrCodeData().isEmpty()) {
                String qrCodeData = pdfGenerationService.generateQRCodeData(ticket);
                ticket.setQrCodeData(qrCodeData);
                ticket = ticketRepository.save(ticket);
            }
            
            // Générer le PDF (régénérer même s'il existe déjà pour être sûr qu'il est à jour)
            String pdfPath = pdfGenerationService.generateTicketPdf(ticket);
            ticket.setPdfPath(pdfPath);
            ticket = ticketRepository.save(ticket);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF pour le ticket {}: {}", ticket.getId(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
        
        // Normaliser le chemin pour gérer les séparateurs Windows/Linux
        String normalizedPath = ticket.getPdfPath().replace("/", File.separator).replace("\\", File.separator);
        File pdfFile = new File(normalizedPath);
        
        // Si le fichier n'existe toujours pas, essayer avec le chemin absolu
        if (!pdfFile.exists()) {
            // Essayer avec le chemin tel quel
            pdfFile = new File(ticket.getPdfPath());
            if (!pdfFile.exists()) {
                // Essayer de trouver le fichier dans le répertoire de travail
                Path workDir = Paths.get(System.getProperty("user.dir"));
                Path possiblePath = workDir.resolve("tickets").resolve("ticket_" + ticket.getNumeroTicket() + ".pdf");
                if (Files.exists(possiblePath)) {
                    pdfFile = possiblePath.toFile();
                } else {
                    log.error("Fichier PDF introuvable. Chemins testés:");
                    log.error("  - {}", normalizedPath);
                    log.error("  - {}", ticket.getPdfPath());
                    log.error("  - {}", possiblePath.toAbsolutePath());
                    throw new RuntimeException("Le fichier PDF n'existe pas: " + ticket.getPdfPath() + 
                                             ". Chemins testés: " + normalizedPath + ", " + possiblePath.toAbsolutePath());
                }
            }
        }
        
        Resource resource = new FileSystemResource(pdfFile);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Receipt_Ticket_" + ticket.getNumeroTicket() + ".pdf\"")
                .body(resource);
    }
}
