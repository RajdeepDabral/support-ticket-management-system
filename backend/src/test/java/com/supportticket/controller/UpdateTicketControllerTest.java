package com.supportticket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.TicketResponse;
import com.supportticket.dto.UpdateTicketCommand;
import com.supportticket.exception.GlobalExceptionHandler;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.service.CommentService;
import com.supportticket.service.TicketService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TicketController.class)
@Import(GlobalExceptionHandler.class)
class UpdateTicketControllerTest {

    private static final String TICKETS_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    @Test
    void updateTicket_updatesTitle() throws Exception {
        stubSuccessfulUpdate();

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated login issue"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated login issue"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(ticketService).updateTicket(eq(1L), any(UpdateTicketCommand.class));
    }

    @Test
    void updateTicket_updatesDescription() throws Exception {
        stubSuccessfulUpdate();

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Original description"));

        verify(ticketService).updateTicket(eq(1L), any(UpdateTicketCommand.class));
    }

    @Test
    void updateTicket_updatesPriority() throws Exception {
        stubSuccessfulUpdate();

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "CRITICAL"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("CRITICAL"));

        verify(ticketService).updateTicket(eq(1L), any(UpdateTicketCommand.class));
    }

    @Test
    void updateTicket_updatesAssignee() throws Exception {
        stubSuccessfulUpdate();

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignee": "new.assignee"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").value("original.assignee"));

        verify(ticketService).updateTicket(eq(1L), any(UpdateTicketCommand.class));
    }

    @Test
    void updateTicket_updatesMultipleFields() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T10:30:00Z");
        when(ticketService.updateTicket(eq(1L), any(UpdateTicketCommand.class))).thenReturn(new TicketResponse(
                1L,
                "Updated login issue",
                "Updated description",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe",
                OffsetDateTime.parse("2026-09-25T10:00:00Z"),
                now));

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated login issue",
                                  "description": "Updated description",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated login issue"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assignee").value("john.doe"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").value("2026-09-25T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-09-25T10:30:00Z"));
    }

    @Test
    void updateTicket_rejectsExplicitNullTitle() throws Exception {
        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Title must not be null."));

        verify(ticketService, never()).updateTicket(any(), any());
    }

    @Test
    void updateTicket_rejectsExplicitNullPriority() throws Exception {
        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Priority must not be null."));

        verify(ticketService, never()).updateTicket(any(), any());
    }

    @Test
    void updateTicket_rejectsBlankTitle() throws Exception {
        when(ticketService.updateTicket(eq(1L), any(UpdateTicketCommand.class)))
                .thenThrow(new com.supportticket.exception.InvalidRequestException("Title must not be blank"));

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Title must not be blank"));
    }

    @Test
    void updateTicket_rejectsBlankDescription() throws Exception {
        when(ticketService.updateTicket(eq(1L), any(UpdateTicketCommand.class)))
                .thenThrow(new com.supportticket.exception.InvalidRequestException("Description must not be blank"));

        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Description must not be blank"));
    }

    @Test
    void updateTicket_rejectsInvalidPriority() throws Exception {
        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "URGENT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Priority must be one of LOW, MEDIUM, HIGH, CRITICAL"));

        verify(ticketService, never()).updateTicket(any(), any());
    }

    @Test
    void updateTicket_rejectsEmptyBody() throws Exception {
        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("At least one updatable field must be provided."));

        verify(ticketService, never()).updateTicket(any(), any());
    }

    @Test
    void updateTicket_rejectsStatusField() throws Exception {
        mockMvc.perform(patch(TICKETS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request contains unsupported fields."));

        verify(ticketService, never()).updateTicket(any(), any());
    }

    @Test
    void updateTicket_returns404WhenTicketNotFound() throws Exception {
        when(ticketService.updateTicket(eq(999L), any(UpdateTicketCommand.class)))
                .thenThrow(new TicketNotFoundException(999L));

        mockMvc.perform(patch(TICKETS_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Ticket with id 999 was not found."));
    }

    private void stubSuccessfulUpdate() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-25T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-09-25T10:30:00Z");
        when(ticketService.updateTicket(eq(1L), any(UpdateTicketCommand.class))).thenReturn(new TicketResponse(
                1L,
                "Updated login issue",
                "Original description",
                TicketPriority.CRITICAL,
                TicketStatus.OPEN,
                "original.assignee",
                createdAt,
                updatedAt));
    }
}
