package com.supportticket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    private static final String CREATE_TICKET_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    @Test
    void createTicket_returnsCreatedWithTicketResponse() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T08:00:00Z");
        TicketResponse response = new TicketResponse(
                1001L,
                "Unable to login",
                "User receives an authentication error when attempting to login.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe",
                now,
                now);
        when(ticketService.createTicket(any())).thenReturn(response);

        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "User receives an authentication error when attempting to login.",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1001))
                .andExpect(jsonPath("$.title").value("Unable to login"))
                .andExpect(jsonPath("$.description")
                        .value("User receives an authentication error when attempting to login."))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.assignee").value("john.doe"))
                .andExpect(jsonPath("$.createdAt").value("2026-09-25T08:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-09-25T08:00:00Z"));

        verify(ticketService).createTicket(any());
    }

    @Test
    void createTicket_rejectsBlankTitle() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Valid description",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.path").value(CREATE_TICKET_URL))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be blank"));
    }

    @Test
    void createTicket_rejectsMissingTitle() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Valid description",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be blank"));
    }

    @Test
    void createTicket_rejectsTitleLongerThan255Characters() throws Exception {
        String longTitle = "a".repeat(256);

        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "title": "%s",
                                  "description": "Valid description",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """, longTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not exceed 255 characters"));
    }

    @Test
    void createTicket_rejectsBlankDescription() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "   ",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.description").value("Description must not be blank"));
    }

    @Test
    void createTicket_rejectsMissingDescription() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "priority": "HIGH",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.description").value("Description must not be blank"));
    }

    @Test
    void createTicket_rejectsMissingPriority() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "Valid description",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.priority").value("Priority is required"));
    }

    @Test
    void createTicket_rejectsInvalidPriority() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "Valid description",
                                  "priority": "URGENT",
                                  "assignee": "john.doe"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Priority must be one of LOW, MEDIUM, HIGH, CRITICAL"));
    }

    @Test
    void createTicket_rejectsBlankAssignee() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "Valid description",
                                  "priority": "HIGH",
                                  "assignee": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.assignee").value("Assignee must not be blank"));
    }

    @Test
    void createTicket_rejectsMissingAssignee() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "Valid description",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.assignee").value("Assignee must not be blank"));
    }

    @Test
    void createTicket_rejectsClientProvidedStatus() throws Exception {
        mockMvc.perform(post(CREATE_TICKET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Unable to login",
                                  "description": "Valid description",
                                  "priority": "HIGH",
                                  "assignee": "john.doe",
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request contains unsupported fields."));
    }
}
