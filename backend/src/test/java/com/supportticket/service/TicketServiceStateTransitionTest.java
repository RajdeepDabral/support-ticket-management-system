package com.supportticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.TicketResponse;
import com.supportticket.exception.InvalidRequestException;
import com.supportticket.exception.InvalidStatusTransitionException;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.mapper.CommentMapper;
import com.supportticket.mapper.TicketMapper;
import com.supportticket.repository.CommentRepository;
import com.supportticket.repository.TicketRepository;
import com.supportticket.state.TicketStateTransitionValidator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TicketServiceStateTransitionTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    private final TicketMapper ticketMapper = new TicketMapper(new CommentMapper());
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final TicketStateTransitionValidator stateTransitionValidator = new TicketStateTransitionValidator();

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(
                ticketRepository, commentRepository, ticketMapper, validator, stateTransitionValidator);
    }

    @ParameterizedTest(name = "ticket {0} -> {1} persists new status")
    @MethodSource("validTransitions")
    void transitionTicketStatus_allowsValidTransitions(TicketStatus from, TicketStatus to) {
        Ticket ticket = persistedTicket(1L, from);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.transitionTicketStatus(1L, to);

        assertThat(response.getStatus()).isEqualTo(to);
        assertThat(ticket.getStatus()).isEqualTo(to);
        verify(ticketRepository).save(ticket);
    }

    @ParameterizedTest(name = "ticket {0} -> {1} is rejected")
    @MethodSource("invalidTransitions")
    void transitionTicketStatus_rejectsInvalidTransitions(TicketStatus from, TicketStatus to) {
        Ticket ticket = persistedTicket(1L, from);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.transitionTicketStatus(1L, to))
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(ticket.getStatus()).isEqualTo(from);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void transitionTicketStatus_throwsWhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.transitionTicketStatus(99L, TicketStatus.IN_PROGRESS))
                .isInstanceOf(TicketNotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void transitionTicketStatus_rejectsNullStatus() {
        assertThatThrownBy(() -> ticketService.transitionTicketStatus(1L, null))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Status is required");

        verify(ticketRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any());
    }

    private static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    private static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN));
    }

    private Ticket persistedTicket(Long id, TicketStatus status) {
        Ticket ticket = new Ticket(
                "Title",
                "Description",
                TicketPriority.MEDIUM,
                status,
                "assignee");
        ReflectionTestUtils.setField(ticket, "id", id);
        ReflectionTestUtils.setField(ticket, "createdAt", OffsetDateTime.now().minusHours(1));
        ReflectionTestUtils.setField(ticket, "updatedAt", OffsetDateTime.now().minusMinutes(30));
        return ticket;
    }
}
