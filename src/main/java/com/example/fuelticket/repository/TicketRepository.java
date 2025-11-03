package com.example.fuelticket.repository;

import com.example.fuelticket.entity.Station;
import com.example.fuelticket.entity.Ticket;
import com.example.fuelticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByStationAndDateApprovisionnement(Station station, LocalDate date);
    List<Ticket> findByStationAndDateApprovisionnement(Station station, LocalDate date);
    List<Ticket> findByCitoyen(User user);
    List<Ticket> findByStation(Station station);
    boolean existsByNumeroTicket(String numeroTicket);
    
    // Méthodes pour vérifier les tickets en attente
    List<Ticket> findByCitoyenAndStatutIn(User user, List<Ticket.Statut> statuts);
    List<Ticket> findByEmailCitoyenAndStatutIn(String emailCitoyen, List<Ticket.Statut> statuts);
    boolean existsByCitoyenAndStatutIn(User user, List<Ticket.Statut> statuts);
    boolean existsByEmailCitoyenAndStatutIn(String emailCitoyen, List<Ticket.Statut> statuts);
    
    // Méthodes pour la gestion des tickets expirés
    List<Ticket> findByStatut(Ticket.Statut statut);
    List<Ticket> findByStatutAndDateCreationBefore(Ticket.Statut statut, LocalDateTime date);
    
    @Query("SELECT t FROM Ticket t WHERE t.statut = :statut AND t.dateExpiration < :now")
    List<Ticket> findExpiredTickets(@Param("statut") Ticket.Statut statut, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Ticket t WHERE t.ancienNumeroTicket = :ancienNumero")
    List<Ticket> findByAncienNumeroTicket(@Param("ancienNumero") String ancienNumero);
    
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.citoyen LEFT JOIN FETCH t.station LEFT JOIN FETCH t.saleSchedule WHERE t.id = :id")
    java.util.Optional<Ticket> findByIdWithCitoyenAndStation(@Param("id") Long id);
    
    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.citoyen LEFT JOIN FETCH t.station")
    List<Ticket> findAllWithCitoyenAndStation();
    
    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.citoyen LEFT JOIN FETCH t.station WHERE t.station = :station")
    List<Ticket> findByStationWithCitoyen(@Param("station") Station station);
    
    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.citoyen LEFT JOIN FETCH t.station WHERE t.citoyen = :citoyen")
    List<Ticket> findByCitoyenWithStation(@Param("citoyen") User citoyen);
    
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.citoyen LEFT JOIN FETCH t.station WHERE t.numeroTicket = :numeroTicket")
    java.util.Optional<Ticket> findByNumeroTicketWithCitoyenAndStation(@Param("numeroTicket") String numeroTicket);
}
