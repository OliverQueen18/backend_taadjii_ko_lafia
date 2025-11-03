package com.example.fuelticket.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketStatusResponse {
    private boolean hasPendingTickets;
    private String message;
    private int pendingCount;
    
    public static TicketStatusResponse withPendingTickets(int count) {
        return TicketStatusResponse.builder()
                .hasPendingTickets(true)
                .message("Vous avez " + count + " ticket(s) en attente")
                .pendingCount(count)
                .build();
    }
    
    public static TicketStatusResponse noPendingTickets() {
        return TicketStatusResponse.builder()
                .hasPendingTickets(false)
                .message("Aucun ticket en attente")
                .pendingCount(0)
                .build();
    }
}
