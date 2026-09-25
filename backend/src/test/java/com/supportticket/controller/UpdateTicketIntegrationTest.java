package com.supportticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.TicketRepository;
import com.supportticket.support.AbstractPostgreSQLContainerTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpdateTicketIntegrationTest extends AbstractPostgreSQLContainerTest {

    private static final String TICKETS_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void updateTicket_partialUpdatePreservesOmittedFields() throws Exception {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Original title",
                "Original description",
                TicketPriority.LOW,
                TicketStatus.IN_PROGRESS,
                "original.assignee"));

        mockMvc.perform(patch(TICKETS_URL + "/" + ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original title"))
                .andExpect(jsonPath("$.description").value("Original description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assignee").value("original.assignee"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Original title");
        assertThat(updated.getDescription()).isEqualTo("Original description");
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(updated.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void updateTicket_preservesCreatedAtAndUpdatesUpdatedAt() throws Exception {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Original title",
                "Original description",
                TicketPriority.MEDIUM,
                TicketStatus.OPEN,
                "original.assignee"));
        OffsetDateTime createdAt = ticket.getCreatedAt();
        OffsetDateTime updatedAtBefore = ticket.getUpdatedAt();

        mockMvc.perform(patch(TICKETS_URL + "/" + ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updatedAtBefore);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }

    @Test
    void updateTicket_rejectsStatusField() throws Exception {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Original title",
                "Original description",
                TicketPriority.MEDIUM,
                TicketStatus.OPEN,
                "original.assignee"));

        mockMvc.perform(patch(TICKETS_URL + "/" + ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request contains unsupported fields."));

        Ticket unchanged = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void updateTicket_returns404ForMissingTicket() throws Exception {
        mockMvc.perform(patch(TICKETS_URL + "/424242")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));
    }
}
