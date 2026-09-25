package com.supportticket.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.TicketResponse;
import com.supportticket.exception.GlobalExceptionHandler;
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
class TicketSearchFilterControllerTest {

    private static final String TICKETS_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    @Test
    void listTickets_passesKeywordAndStatusToService() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T08:00:00Z");
        when(ticketService.listTickets(eq("login"), eq(TicketStatus.OPEN))).thenReturn(List.of(
                new TicketResponse(
                        1L,
                        "Login issue",
                        "User cannot login",
                        TicketPriority.HIGH,
                        TicketStatus.OPEN,
                        "rajdeep",
                        now,
                        now)));

        mockMvc.perform(get(TICKETS_URL).param("keyword", "login").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Login issue"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        verify(ticketService).listTickets("login", TicketStatus.OPEN);
    }

    @Test
    void listTickets_rejectsInvalidStatus() throws Exception {
        mockMvc.perform(get(TICKETS_URL).param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED"));
    }

    @Test
    void listTickets_returnsEmptyArrayFromService() throws Exception {
        when(ticketService.listTickets(eq("missing"), isNull())).thenReturn(List.of());

        mockMvc.perform(get(TICKETS_URL).param("keyword", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
