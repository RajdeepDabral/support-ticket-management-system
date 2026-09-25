package com.supportticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.CreateTicketRequest;
import com.supportticket.dto.TicketResponse;
import com.supportticket.dto.UpdateTicketCommand;
import com.supportticket.exception.InvalidRequestException;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.mapper.TicketMapper;
import com.supportticket.repository.TicketRepository;
import com.supportticket.state.TicketStateTransitionValidator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    private final TicketMapper ticketMapper = new TicketMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final TicketStateTransitionValidator stateTransitionValidator = new TicketStateTransitionValidator();

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(
                ticketRepository, ticketMapper, validator, stateTransitionValidator);
    }

    @Test
    void createTicket_setsInitialStatusToOpen() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ReflectionTestUtils.setField(ticket, "id", 1L);
            ReflectionTestUtils.setField(ticket, "createdAt", OffsetDateTime.now());
            ReflectionTestUtils.setField(ticket, "updatedAt", OffsetDateTime.now());
            return ticket;
        });

        CreateTicketRequest request = new CreateTicketRequest(
                "Unable to login",
                "User cannot access the application.",
                TicketPriority.HIGH,
                "john.doe");

        TicketResponse response = ticketService.createTicket(request);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.OPEN);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void createTicket_persistsValidTicket() {
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ReflectionTestUtils.setField(ticket, "id", 42L);
            ReflectionTestUtils.setField(ticket, "createdAt", OffsetDateTime.now());
            ReflectionTestUtils.setField(ticket, "updatedAt", OffsetDateTime.now());
            return ticket;
        });

        CreateTicketRequest request = new CreateTicketRequest(
                "API timeout",
                "Requests are timing out.",
                TicketPriority.MEDIUM,
                "jane.doe");

        TicketResponse response = ticketService.createTicket(request);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getTitle()).isEqualTo("API timeout");
        assertThat(response.getDescription()).isEqualTo("Requests are timing out.");
        assertThat(response.getPriority()).isEqualTo(TicketPriority.MEDIUM);
        assertThat(response.getAssignee()).isEqualTo("jane.doe");
    }

    @Test
    void createTicket_rejectsBlankTitle() {
        CreateTicketRequest request = new CreateTicketRequest(
                "  ",
                "Valid description",
                TicketPriority.LOW,
                "john.doe");

        assertThatThrownBy(() -> ticketService.createTicket(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Title must not be blank");
    }

    @Test
    void createTicket_rejectsMissingPriority() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Valid title",
                "Valid description",
                null,
                "john.doe");

        assertThatThrownBy(() -> ticketService.createTicket(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Priority is required");
    }

    @Test
    void getTicket_returnsExistingTicket() {
        Ticket ticket = persistedTicket(10L, TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.getTicket(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void getTicket_throwsWhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(99L))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateTicket_updatesTitle() {
        Ticket ticket = persistedTicket(1L, TicketStatus.OPEN);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicket(
                1L,
                UpdateTicketCommand.builder().title("Updated title").build());

        assertThat(response.getTitle()).isEqualTo("Updated title");
        assertThat(response.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void updateTicket_updatesDescription() {
        Ticket ticket = persistedTicket(1L, TicketStatus.OPEN);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicket(
                1L,
                UpdateTicketCommand.builder().description("Updated description").build());

        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateTicket_updatesPriority() {
        Ticket ticket = persistedTicket(1L, TicketStatus.OPEN);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicket(
                1L,
                UpdateTicketCommand.builder().priority(TicketPriority.CRITICAL).build());

        assertThat(response.getPriority()).isEqualTo(TicketPriority.CRITICAL);
    }

    @Test
    void updateTicket_updatesAssignee() {
        Ticket ticket = persistedTicket(1L, TicketStatus.OPEN);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicket(
                1L,
                UpdateTicketCommand.builder().assignee("new.assignee").build());

        assertThat(response.getAssignee()).isEqualTo("new.assignee");
    }

    @Test
    void updateTicket_doesNotChangeStatus() {
        Ticket ticket = persistedTicket(1L, TicketStatus.RESOLVED);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicket(
                1L,
                UpdateTicketCommand.builder()
                        .title("Updated title")
                        .priority(TicketPriority.HIGH)
                        .build());

        assertThat(response.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void updateTicket_throwsWhenTicketNotFound() {
        when(ticketRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.updateTicket(
                404L,
                UpdateTicketCommand.builder().title("Updated title").build()))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void updateTicket_rejectsEmptyUpdate() {
        Ticket ticket = persistedTicket(1L, TicketStatus.OPEN);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateTicket(1L, UpdateTicketCommand.builder().build()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("At least one updatable field must be provided");
    }

    @Test
    void updateTicket_rejectsBlankTitle() {
        Ticket ticket = persistedTicket(1L, TicketStatus.OPEN);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateTicket(
                1L,
                UpdateTicketCommand.builder().title("   ").build()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Title must not be blank");
    }

    private Ticket persistedTicket(Long id, TicketStatus status) {
        Ticket ticket = new Ticket(
                "Original title",
                "Original description",
                TicketPriority.MEDIUM,
                status,
                "original.assignee");
        ReflectionTestUtils.setField(ticket, "id", id);
        ReflectionTestUtils.setField(ticket, "createdAt", OffsetDateTime.now().minusHours(1));
        ReflectionTestUtils.setField(ticket, "updatedAt", OffsetDateTime.now().minusMinutes(30));
        return ticket;
    }
}
