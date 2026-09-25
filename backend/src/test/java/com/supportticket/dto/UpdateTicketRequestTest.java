package com.supportticket.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportticket.domain.TicketPriority;
import com.supportticket.exception.InvalidRequestException;

import org.junit.jupiter.api.Test;

class UpdateTicketRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesPartialUpdate() throws Exception {
        UpdateTicketRequest request = objectMapper.readValue(
                """
                {
                  "priority": "HIGH"
                }
                """,
                UpdateTicketRequest.class);

        assertThat(request.getTitle()).isEmpty();
        assertThat(request.getPriority()).contains(TicketPriority.HIGH);

        UpdateTicketCommand command = request.toCommand();
        assertThat(command.getPriority()).contains(TicketPriority.HIGH);
        assertThat(command.getTitle()).isEmpty();
    }

    @Test
    void rejectsExplicitNullValues() {
        assertThatThrownBy(() -> objectMapper.readValue(
                """
                {
                  "title": null
                }
                """,
                UpdateTicketRequest.class))
                .hasMessageContaining("Title must not be null");
    }

    @Test
    void rejectsEmptyUpdateBody() throws Exception {
        UpdateTicketRequest request = objectMapper.readValue("{}", UpdateTicketRequest.class);

        assertThatThrownBy(request::toCommand)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("At least one updatable field must be provided.");
    }
}
