package com.supportticket.controller;

import java.util.List;

import com.supportticket.dto.CommentResponse;
import com.supportticket.dto.CreateCommentRequest;
import com.supportticket.dto.CreateTicketRequest;
import com.supportticket.dto.TicketDetailResponse;
import com.supportticket.dto.TicketResponse;
import com.supportticket.dto.UpdateTicketRequest;
import com.supportticket.dto.UpdateTicketStatusRequest;
import com.supportticket.service.CommentService;
import com.supportticket.service.TicketService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    public TicketController(TicketService ticketService, CommentService commentService) {
        this.ticketService = ticketService;
        this.commentService = commentService;
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

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody UpdateTicketRequest request) {
        TicketResponse response = ticketService.updateTicket(ticketId, request.toCommand());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> transitionTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
        TicketResponse response = ticketService.transitionTicketStatus(ticketId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
