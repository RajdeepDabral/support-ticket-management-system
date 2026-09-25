package com.supportticket.service;

import java.util.List;

import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.CreateTicketRequest;
import com.supportticket.dto.TicketDetailResponse;
import com.supportticket.dto.TicketResponse;
import com.supportticket.dto.UpdateTicketCommand;

public interface TicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    List<TicketResponse> listTickets();

    TicketDetailResponse getTicket(Long ticketId);

    TicketResponse updateTicket(Long ticketId, UpdateTicketCommand command);

    TicketResponse transitionTicketStatus(Long ticketId, TicketStatus requestedStatus);
}
