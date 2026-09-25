package com.supportticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.CommentRepository;
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
class TicketRetrievalIntegrationTest extends AbstractPostgreSQLContainerTest {

    private static final String TICKETS_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void listTickets_returnsEmptyArrayWhenNoTicketsExist() throws Exception {
        mockMvc.perform(get(TICKETS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listTickets_returnsPersistedTickets() throws Exception {
        ticketRepository.save(new Ticket(
                "Unable to login",
                "User cannot access the application.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe"));
        ticketRepository.save(new Ticket(
                "API timeout",
                "Requests are timing out.",
                TicketPriority.MEDIUM,
                TicketStatus.IN_PROGRESS,
                "jane.doe"));

        mockMvc.perform(get(TICKETS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTicket_returnsTicketWithComments() throws Exception {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Unable to login",
                "User cannot access the application.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe"));
        commentRepository.save(new Comment(ticket, "Investigating the authentication issue.", "jane.doe"));

        mockMvc.perform(get(TICKETS_URL + "/" + ticket.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.getId()))
                .andExpect(jsonPath("$.title").value("Unable to login"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].content").value("Investigating the authentication issue."))
                .andExpect(jsonPath("$.comments[0].author").value("jane.doe"));
    }

    @Test
    void getTicket_returns404ForUnknownTicket() throws Exception {
        mockMvc.perform(get(TICKETS_URL + "/424242"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));
    }

    @Test
    void createTicketStillWorksAfterRetrievalEndpointsAdded() throws Exception {
        mockMvc.perform(post(TICKETS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New ticket",
                                  "description": "Created after retrieval endpoints were added.",
                                  "priority": "LOW",
                                  "assignee": "reviewer"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        assertThat(ticketRepository.findAll()).hasSize(1);
    }
}
