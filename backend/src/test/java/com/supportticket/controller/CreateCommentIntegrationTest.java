package com.supportticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class CreateCommentIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void addComment_createsCommentWithGeneratedIdAndCreatedAt() throws Exception {
        Ticket ticket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I have investigated the issue.",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ticketId").value(ticket.getId()))
                .andExpect(jsonPath("$.content").value("I have investigated the issue."))
                .andExpect(jsonPath("$.author").value("rajdeep"))
                .andExpect(jsonPath("$.createdAt").exists());

        var comments = commentRepository.findByTicket_Id(ticket.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isNotNull();
        assertThat(comments.get(0).getContent()).isEqualTo("I have investigated the issue.");
        assertThat(comments.get(0).getAuthor()).isEqualTo("rajdeep");
        assertThat(comments.get(0).getCreatedAt()).isNotNull();
        assertThat(comments.get(0).getTicket().getId()).isEqualTo(ticket.getId());
    }

    @Test
    void addComment_returns404ForMissingTicket() throws Exception {
        mockMvc.perform(post(commentsUrl(424242L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I have investigated the issue.",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));

        assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    void addComment_rejectsBlankContent() throws Exception {
        Ticket ticket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(commentRepository.findByTicket_Id(ticket.getId())).isEmpty();
    }

    @Test
    void addComment_rejectsNullContent() throws Exception {
        Ticket ticket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": null,
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(commentRepository.findByTicket_Id(ticket.getId())).isEmpty();
    }

    @Test
    void addComment_rejectsBlankAuthor() throws Exception {
        Ticket ticket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Some comment",
                                  "author": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(commentRepository.findByTicket_Id(ticket.getId())).isEmpty();
    }

    @Test
    void addComment_rejectsNullAuthor() throws Exception {
        Ticket ticket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Some comment",
                                  "author": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(commentRepository.findByTicket_Id(ticket.getId())).isEmpty();
    }

    @Test
    void addComment_appearsInTicketDetailResponse() throws Exception {
        Ticket ticket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "I have investigated the issue.",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tickets/" + ticket.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].content").value("I have investigated the issue."))
                .andExpect(jsonPath("$.comments[0].author").value("rajdeep"))
                .andExpect(jsonPath("$.comments[0].id").isNumber())
                .andExpect(jsonPath("$.comments[0].createdAt").exists());
    }

    @Test
    void addComment_ignoresClientControlledIdAndUsesUrlTicketId() throws Exception {
        Ticket ticket = saveTicket();
        Ticket otherTicket = saveTicket();

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "content": "Some comment",
                                  "author": "rajdeep"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(commentsUrl(ticket.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketId": %d,
                                  "content": "Some comment",
                                  "author": "rajdeep"
                                }
                                """.formatted(otherTicket.getId())))
                .andExpect(status().isBadRequest());

        assertThat(commentRepository.findByTicket_Id(ticket.getId())).isEmpty();
        assertThat(commentRepository.findByTicket_Id(otherTicket.getId())).isEmpty();
    }

    private Ticket saveTicket() {
        return ticketRepository.save(new Ticket(
                "Login issue",
                "User cannot login",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "rajdeep"));
    }

    private String commentsUrl(Long ticketId) {
        return "/api/v1/tickets/" + ticketId + "/comments";
    }
}
