package com.supportticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.TicketRepository;
import com.supportticket.support.AbstractPostgreSQLContainerTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketStatusTransitionIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @ParameterizedTest(name = "{0} -> {1} persists new status")
    @MethodSource("validTransitions")
    void transitionTicketStatus_allowsValidTransitions(TicketStatus from, TicketStatus to) throws Exception {
        Ticket ticket = saveTicket(from);

        mockMvc.perform(patch(statusUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + to + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.getId()))
                .andExpect(jsonPath("$.status").value(to.name()));

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(to);
    }

    @ParameterizedTest(name = "{0} -> {1} returns 409 and leaves status unchanged")
    @MethodSource("invalidTransitions")
    void transitionTicketStatus_rejectsInvalidTransitions(TicketStatus from, TicketStatus to) throws Exception {
        Ticket ticket = saveTicket(from);

        mockMvc.perform(patch(statusUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + to + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"))
                .andExpect(jsonPath("$.message")
                        .value("Ticket cannot transition from " + from + " to " + to + "."));

        Ticket unchanged = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(from);
    }

    @ParameterizedTest(name = "{0} -> {0} returns 409")
    @MethodSource("sameStatusTransitions")
    void transitionTicketStatus_rejectsSameStatusTransitions(TicketStatus status) throws Exception {
        Ticket ticket = saveTicket(status);

        mockMvc.perform(patch(statusUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + status + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        Ticket unchanged = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(status);
    }

    @Test
    void transitionTicketStatus_rejectsInvalidStatusValue() throws Exception {
        Ticket ticket = saveTicket(TicketStatus.OPEN);

        mockMvc.perform(patch(statusUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void transitionTicketStatus_returns404ForMissingTicket() throws Exception {
        mockMvc.perform(patch(statusUrl(424242L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));
    }

    @Test
    void transitionTicketStatus_preservesCreatedAtAndUpdatesUpdatedAt() throws Exception {
        Ticket ticket = saveTicket(TicketStatus.OPEN);
        OffsetDateTime createdAt = ticket.getCreatedAt();
        OffsetDateTime updatedAtBefore = ticket.getUpdatedAt();

        mockMvc.perform(patch(statusUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updatedAtBefore);
        assertThat(updated.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    private Ticket saveTicket(TicketStatus status) {
        return ticketRepository.save(new Ticket(
                "Login issue",
                "User cannot login",
                TicketPriority.HIGH,
                status,
                "rajdeep"));
    }

    private String statusUrl(Long ticketId) {
        return "/api/v1/tickets/" + ticketId + "/status";
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
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN));
    }

    private static Stream<Arguments> sameStatusTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CANCELLED));
    }
}
