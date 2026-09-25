package com.supportticket.exception;

public class TicketNotFoundException extends RuntimeException {

    private final Long ticketId;

    public TicketNotFoundException(Long ticketId) {
        super("Ticket with id " + ticketId + " was not found.");
        this.ticketId = ticketId;
    }

    public Long getTicketId() {
        return ticketId;
    }
}
