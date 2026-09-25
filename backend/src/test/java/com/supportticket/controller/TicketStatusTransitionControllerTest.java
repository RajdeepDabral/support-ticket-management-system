package com.supportticket.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.TicketResponse;
import com.supportticket.exception.GlobalExceptionHandler;
import com.supportticket.exception.InvalidStatusTransitionException;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.service.CommentService;
import com.supportticket.service.TicketService;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketStatusTransitionControllerTest {

    private static final String STATUS_URL = "/api/v1/tickets/1/status";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    @ParameterizedTest(name = "{0} -> {1} returns 200")
    @MethodSource("validTransitions")
    void transitionTicketStatus_allowsValidTransitions(TicketStatus from, TicketStatus to) throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T10:30:00Z");
        when(ticketService.transitionTicketStatus(eq(1L), eq(to))).thenReturn(new TicketResponse(
                1L,
                "Login issue",
                "User cannot login",
                TicketPriority.HIGH,
                to,
                "rajdeep",
                OffsetDateTime.parse("2026-09-25T10:00:00Z"),
                now));

        mockMvc.perform(patch(STATUS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + to + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(to.name()));

        verify(ticketService).transitionTicketStatus(1L, to);
    }

    @ParameterizedTest(name = "{0} -> {1} returns 409")
    @MethodSource("invalidTransitions")
    void transitionTicketStatus_rejectsInvalidTransitions(TicketStatus from, TicketStatus to) throws Exception {
        when(ticketService.transitionTicketStatus(eq(1L), eq(to)))
                .thenThrow(new InvalidStatusTransitionException(from, to));

        mockMvc.perform(patch(STATUS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + to + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"))
                .andExpect(jsonPath("$.message")
                        .value("Ticket cannot transition from " + from + " to " + to + "."));
    }

    @ParameterizedTest(name = "{0} -> {0} returns 409")
    @MethodSource("sameStatusTransitions")
    void transitionTicketStatus_rejectsSameStatusTransitions(TicketStatus status) throws Exception {
        when(ticketService.transitionTicketStatus(eq(1L), eq(status)))
                .thenThrow(new InvalidStatusTransitionException(status, status));

        mockMvc.perform(patch(STATUS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + status + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    @org.junit.jupiter.api.Test
    void transitionTicketStatus_rejectsInvalidStatusValue() throws Exception {
        mockMvc.perform(patch(STATUS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED"));
    }

    @org.junit.jupiter.api.Test
    void transitionTicketStatus_returns404WhenTicketNotFound() throws Exception {
        when(ticketService.transitionTicketStatus(eq(999L), eq(TicketStatus.IN_PROGRESS)))
                .thenThrow(new TicketNotFoundException(999L));

        mockMvc.perform(patch("/api/v1/tickets/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));
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
