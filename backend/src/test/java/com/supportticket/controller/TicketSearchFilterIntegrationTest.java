package com.supportticket.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.repository.TicketRepository;
import com.supportticket.support.AbstractPostgreSQLContainerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketSearchFilterIntegrationTest extends AbstractPostgreSQLContainerTest {

    private static final String TICKETS_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        ticketRepository.save(new Ticket(
                "Login issue",
                "User cannot access the application.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe"));
        ticketRepository.save(new Ticket(
                "API timeout",
                "Unable to LOGIN to application during peak hours.",
                TicketPriority.MEDIUM,
                TicketStatus.IN_PROGRESS,
                "jane.doe"));
        ticketRepository.save(new Ticket(
                "Payment failure",
                "Payment gateway returns an error.",
                TicketPriority.CRITICAL,
                TicketStatus.RESOLVED,
                "alex.smith"));
        ticketRepository.save(new Ticket(
                "Closed ticket",
                "Resolved and closed.",
                TicketPriority.LOW,
                TicketStatus.CLOSED,
                "ops.team"));
        ticketRepository.save(new Ticket(
                "Cancelled ticket",
                "Cancelled before work started.",
                TicketPriority.MEDIUM,
                TicketStatus.CANCELLED,
                "reviewer"));
    }

    @Test
    void listTickets_returnsAllTicketsWithNoFilters() throws Exception {
        mockMvc.perform(get(TICKETS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void listTickets_matchesKeywordInTitle() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "Login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Login issue"));
    }

    @Test
    void listTickets_matchesKeywordInDescription() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "gateway"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Payment failure"));
    }

    @Test
    void listTickets_isCaseInsensitive() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listTickets_supportsSubstringMatching() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Payment failure"));
    }

    @Test
    void listTickets_returnsEmptyArrayWhenKeywordDoesNotMatch() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listTickets_filtersByOpenStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void listTickets_filtersByInProgressStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void listTickets_filtersByResolvedStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("RESOLVED"));
    }

    @Test
    void listTickets_filtersByClosedStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CLOSED"));
    }

    @Test
    void listTickets_filtersByCancelledStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    void listTickets_rejectsInvalidStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void listTickets_appliesKeywordAndStatusTogether() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "login").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Login issue"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void listTickets_excludesKeywordMatchWithWrongStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "login").param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listTickets_returnsEmptyArrayWhenStatusHasNoTickets() throws Exception {
        ticketRepository.deleteAll();
        ticketRepository.save(new Ticket(
                "Only open",
                "Description",
                TicketPriority.LOW,
                TicketStatus.OPEN,
                "user"));

        mockMvc.perform(get(TICKETS_URL).param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listTickets_treatsEmptyKeywordAsNoFilter() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void listTickets_trimsWhitespaceKeyword() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "  login  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listTickets_treatsWhitespaceOnlyKeywordAsNoFilter() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void listTickets_returnsTicketResponseFields() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("keyword", "Login issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value("Login issue"))
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].assignee").value("john.doe"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }
}
