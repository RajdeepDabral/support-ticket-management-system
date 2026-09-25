package com.supportticket.service;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.CreateTicketRequest;
import com.supportticket.dto.TicketResponse;
import com.supportticket.dto.UpdateTicketCommand;
import com.supportticket.exception.InvalidRequestException;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.mapper.TicketMapper;
import com.supportticket.repository.TicketRepository;
import com.supportticket.state.TicketStateTransitionValidator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final Validator validator;
    private final TicketStateTransitionValidator stateTransitionValidator;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            TicketMapper ticketMapper,
            Validator validator,
            TicketStateTransitionValidator stateTransitionValidator) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.validator = validator;
        this.stateTransitionValidator = stateTransitionValidator;
    }

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        validateCreateRequest(request);

        Ticket ticket = new Ticket(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getPriority(),
                TicketStatus.OPEN,
                request.getAssignee().trim());

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long ticketId, UpdateTicketCommand command) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!command.hasUpdates()) {
            throw new InvalidRequestException("At least one updatable field must be provided.");
        }

        TicketStatus statusBeforeUpdate = ticket.getStatus();
        applyUpdates(ticket, command);

        if (ticket.getStatus() != statusBeforeUpdate) {
            throw new InvalidRequestException("Status cannot be changed through ticket update.");
        }

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse transitionTicketStatus(Long ticketId, TicketStatus requestedStatus) {
        if (requestedStatus == null) {
            throw new InvalidRequestException("Status is required.");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        stateTransitionValidator.validate(ticket.getStatus(), requestedStatus);
        ticket.setStatus(requestedStatus);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private void validateCreateRequest(CreateTicketRequest request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new InvalidRequestException(formatViolations(violations));
        }
    }

    private void applyUpdates(Ticket ticket, UpdateTicketCommand command) {
        command.getTitle().ifPresent(title -> {
            if (title.isBlank()) {
                throw new InvalidRequestException("Title must not be blank");
            }
            if (title.length() > 255) {
                throw new InvalidRequestException("Title must not exceed 255 characters");
            }
            ticket.setTitle(title.trim());
        });

        command.getDescription().ifPresent(description -> {
            if (description.isBlank()) {
                throw new InvalidRequestException("Description must not be blank");
            }
            ticket.setDescription(description.trim());
        });

        command.getPriority().ifPresent(ticket::setPriority);

        command.getAssignee().ifPresent(assignee -> {
            if (assignee.isBlank()) {
                throw new InvalidRequestException("Assignee must not be blank");
            }
            if (assignee.length() > 255) {
                throw new InvalidRequestException("Assignee must not exceed 255 characters");
            }
            ticket.setAssignee(assignee.trim());
        });
    }

    private String formatViolations(java.util.Set<ConstraintViolation<CreateTicketRequest>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .reduce((first, second) -> first + "; " + second)
                .orElse("Request validation failed.");
    }
}
