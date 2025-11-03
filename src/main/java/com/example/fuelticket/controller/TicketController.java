package com.example.fuelticket.controller;

import com.example.fuelticket.dto.TicketDto;
import com.example.fuelticket.dto.CashTicketRequest;
import com.example.fuelticket.entity.Ticket;
import com.example.fuelticket.service.AuthService;
import com.example.fuelticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management APIs")
public class TicketController {
    private final TicketService ticketService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create ticket", description = "Create a new fuel ticket")
    @PreAuthorize("hasRole('CITOYEN')")
    public ResponseEntity<TicketDto> creerTicket(@RequestParam Long stationId,
                                                @RequestParam String typeCarburant,
                                                @RequestParam Double quantite,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateApprovisionnement) {
        var currentUser = authService.getCurrentUser();
        TicketDto ticket = ticketService.creerTicket(currentUser.getId(), stationId, typeCarburant, quantite, dateApprovisionnement);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping
    @Operation(summary = "Get all tickets", description = "Retrieve all tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDto>> getAllTickets() {
        List<TicketDto> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieve a ticket by its ID")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable Long id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/by-number/{numeroTicket}")
    @Operation(summary = "Get ticket by number", description = "Retrieve a ticket by its numeroTicket")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<TicketDto> getTicketByNumeroTicket(@PathVariable String numeroTicket) {
        TicketDto ticket = ticketService.getTicketByNumeroTicket(numeroTicket);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/my-tickets")
    @Operation(summary = "Get my tickets", description = "Get current user's tickets")
    @PreAuthorize("hasRole('CITOYEN')")
    public ResponseEntity<List<TicketDto>> getMyTickets() {
        var currentUser = authService.getCurrentUser();
        List<TicketDto> tickets = ticketService.getTicketsByUser(currentUser.getId());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get tickets by station", description = "Get all tickets for a specific station")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<List<TicketDto>> getTicketsByStation(@PathVariable Long stationId) {
        List<TicketDto> tickets = ticketService.getTicketsByStation(stationId);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update ticket status", description = "Update the status of a ticket. Citizens can only cancel (ANNULE) their own tickets with status EN_ATTENTE.")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN') or hasRole('CITOYEN')")
    public ResponseEntity<TicketDto> updateTicketStatus(@PathVariable Long id, 
                                                       @RequestParam Ticket.Statut status) {
        var currentUser = authService.getCurrentUser();
        TicketDto ticket = ticketService.updateTicketStatus(id, status, currentUser.getId());
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/cash")
    @Operation(summary = "Create cash ticket", description = "Create a new fuel ticket paid in cash by station manager")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<TicketDto> creerTicketEspeces(@Valid @RequestBody CashTicketRequest request) {
        TicketDto ticket = ticketService.creerTicketEspeces(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/my-tickets/pending")
    @Operation(summary = "Get my pending tickets", description = "Get current user's pending tickets (EN_ATTENTE or VALIDE)")
    @PreAuthorize("hasRole('CITOYEN')")
    public ResponseEntity<List<TicketDto>> getMyPendingTickets() {
        var currentUser = authService.getCurrentUser();
        List<TicketDto> tickets = ticketService.getTicketsEnAttenteByUser(currentUser.getId());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/pending-by-email")
    @Operation(summary = "Get pending tickets by email", description = "Get pending tickets for a specific email (for cash tickets)")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<List<TicketDto>> getPendingTicketsByEmail(@RequestParam String email) {
        List<TicketDto> tickets = ticketService.getTicketsEnAttenteByEmail(email);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/check-pending")
    @Operation(summary = "Check if user has pending tickets", description = "Check if current user has any pending tickets")
    @PreAuthorize("hasRole('CITOYEN')")
    public ResponseEntity<Boolean> checkPendingTickets() {
        var currentUser = authService.getCurrentUser();
        boolean hasPending = ticketService.hasTicketEnAttente(currentUser.getId());
        return ResponseEntity.ok(hasPending);
    }

    @GetMapping("/check-pending-email")
    @Operation(summary = "Check if email has pending tickets", description = "Check if an email has any pending tickets")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> checkPendingTicketsByEmail(@RequestParam String email) {
        boolean hasPending = ticketService.hasTicketEnAttenteByEmail(email);
        return ResponseEntity.ok(hasPending);
    }

    @PutMapping("/{id}/serve")
    @Operation(summary = "Mark ticket as served", description = "Mark a ticket as served (SERVI status)")
    @PreAuthorize("hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<TicketDto> markTicketAsServed(@PathVariable Long id) {
        var currentUser = authService.getCurrentUser();
        TicketDto ticket = ticketService.updateTicketStatus(id, Ticket.Statut.SERVI, currentUser.getId());
        return ResponseEntity.ok(ticket);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket", description = "Delete a ticket (only if status is EN_ATTENTE and user is the owner or ADMIN)")
    @PreAuthorize("hasRole('CITOYEN') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        var currentUser = authService.getCurrentUser();
        ticketService.deleteTicket(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download ticket PDF", description = "Download the PDF receipt for a ticket")
    @PreAuthorize("hasRole('CITOYEN') or hasRole('STATION') or hasRole('ADMIN')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadTicketPdf(@PathVariable Long id) {
        return ticketService.downloadTicketPdf(id);
    }
}
