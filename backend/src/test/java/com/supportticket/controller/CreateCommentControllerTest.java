package com.supportticket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import com.supportticket.dto.CommentResponse;
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
class CreateCommentControllerTest {

    private static final String COMMENTS_URL = "/api/v1/tickets/1/comments";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    @Test
    void addComment_returnsCreatedWithCommentResponse() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-25T11:30:00Z");
        when(commentService.addComment(eq(1L), any())).thenReturn(new CommentResponse(
                10L,
                1L,
                "I have investigated the issue.",
                "rajdeep",
                now));

        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I have investigated the issue.",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.content").value("I have investigated the issue."))
                .andExpect(jsonPath("$.author").value("rajdeep"))
                .andExpect(jsonPath("$.createdAt").value("2026-09-25T11:30:00Z"));

        verify(commentService).addComment(eq(1L), any());
    }

    @Test
    void addComment_returns404WhenTicketNotFound() throws Exception {
        when(commentService.addComment(eq(999L), any()))
                .thenThrow(new TicketNotFoundException(999L));

        mockMvc.perform(post("/api/v1/tickets/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I have investigated the issue.",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));
    }

    @Test
    void addComment_rejectsBlankContent() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.content").value("Content must not be blank"));
    }

    @Test
    void addComment_rejectsNullContent() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": null,
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.content").value("Content must not be blank"));
    }

    @Test
    void addComment_rejectsBlankAuthor() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Some comment",
                                  "author": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.author").value("Author must not be blank"));
    }

    @Test
    void addComment_rejectsNullAuthor() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Some comment",
                                  "author": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.author").value("Author must not be blank"));
    }

    @Test
    void addComment_rejectsClientControlledId() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "content": "Some comment",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void addComment_rejectsClientControlledCreatedAt() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Some comment",
                                  "author": "rajdeep",
                                  "createdAt": "2020-01-01T00:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void addComment_rejectsClientControlledTicketId() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketId": 999,
                                  "content": "Some comment",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
