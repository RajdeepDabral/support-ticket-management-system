package com.supportticket.mapper;

import com.supportticket.domain.Ticket;
import com.supportticket.dto.TicketResponse;

import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}
