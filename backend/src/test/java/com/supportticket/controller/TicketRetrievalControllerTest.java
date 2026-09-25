package com.supportticket.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.CommentResponse;
import com.supportticket.dto.TicketDetailResponse;
import com.supportticket.dto.TicketResponse;
import com.supportticket.exception.GlobalExceptionHandler;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.service.CommentService;
import com.supportticket.service.TicketService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketRetrievalControllerTest {

    private static final String TICKETS_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    @Test
    void listTickets_returns200WithTickets() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T08:00:00Z");
        when(ticketService.listTickets()).thenReturn(List.of(
                new TicketResponse(
                        1001L,
                        "Unable to login",
                        "User receives an authentication error when attempting to login.",
                        TicketPriority.HIGH,
                        TicketStatus.OPEN,
                        "john.doe",
                        now,
                        now)));

        mockMvc.perform(get(TICKETS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1001))
                .andExpect(jsonPath("$[0].title").value("Unable to login"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].assignee").value("john.doe"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-09-25T08:00:00Z"));

        verify(ticketService).listTickets();
    }

    @Test
    void listTickets_returns200WithEmptyArrayWhenNoTicketsExist() throws Exception {
        when(ticketService.listTickets()).thenReturn(List.of());

        mockMvc.perform(get(TICKETS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTicket_returns200WithTicketDetailsAndComments() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T08:15:00Z");
        when(ticketService.getTicket(1001L)).thenReturn(new TicketDetailResponse(
                1001L,
                "Unable to login",
                "User receives an authentication error when attempting to login.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe",
                now,
                now,
                List.of(new CommentResponse(
                        501L,
                        1001L,
                        "Investigating the authentication issue.",
                        "jane.doe",
                        now))));

        mockMvc.perform(get(TICKETS_URL + "/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.title").value("Unable to login"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].id").value(501))
                .andExpect(jsonPath("$.comments[0].content").value("Investigating the authentication issue."))
                .andExpect(jsonPath("$.comments[0].author").value("jane.doe"))
                .andExpect(jsonPath("$.comments[0].createdAt").value("2026-09-25T08:15:00Z"));

        verify(ticketService).getTicket(1001L);
    }

    @Test
    void getTicket_returns404WhenTicketNotFound() throws Exception {
        when(ticketService.getTicket(9999L)).thenThrow(new TicketNotFoundException(9999L));

        mockMvc.perform(get(TICKETS_URL + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ticket with id 9999 was not found."))
                .andExpect(jsonPath("$.path").value("/api/v1/tickets/9999"));
    }
}
