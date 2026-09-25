package com.supportticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class CreateTicketIntegrationTest extends AbstractPostgreSQLContainerTest {

    private static final String CREATE_TICKET_URL = "/api/v1/tickets";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void createTicket_persistsTicketWithOpenStatusAndGeneratedId() throws Exception {
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
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Unable to login"))
                .andExpect(jsonPath("$.description")
                        .value("User receives an authentication error when attempting to login."))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.assignee").value("john.doe"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        var tickets = ticketRepository.findAll();
        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getId()).isNotNull();
        assertThat(tickets.get(0).getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(tickets.get(0).getTitle()).isEqualTo("Unable to login");
        assertThat(tickets.get(0).getAssignee()).isEqualTo("john.doe");
        assertThat(tickets.get(0).getCreatedAt()).isNotNull();
        assertThat(tickets.get(0).getUpdatedAt()).isNotNull();
    }
}
