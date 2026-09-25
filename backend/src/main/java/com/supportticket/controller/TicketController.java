package com.supportticket.controller;

import java.util.List;

import com.supportticket.dto.CreateTicketRequest;
import com.supportticket.dto.TicketDetailResponse;
import com.supportticket.dto.TicketResponse;
import com.supportticket.service.TicketService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listTickets() {
        return ResponseEntity.ok(ticketService.listTickets());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicket(ticketId));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
