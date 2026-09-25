package com.supportticket.dto;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.supportticket.domain.TicketPriority;

public class UpdateTicketRequestDeserializer extends JsonDeserializer<UpdateTicketRequest> {

    private static final Set<String> ALLOWED_FIELDS = Set.of("title", "description", "priority", "assignee");

    @Override
    public UpdateTicketRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();
        if (!node.isObject()) {
            throw JsonMappingException.from(parser, "Update request must be a JSON object.");
        }

        for (String fieldName : iterable(node.fieldNames())) {
            if (!ALLOWED_FIELDS.contains(fieldName)) {
                throw JsonMappingException.from(parser, "Request contains unsupported fields.");
            }
        }

        UpdateTicketRequest request = new UpdateTicketRequest();

        if (node.has("title")) {
            request.setTitle(readRequiredTextField(parser, node.get("title"), "Title"));
        }
        if (node.has("description")) {
            request.setDescription(readRequiredTextField(parser, node.get("description"), "Description"));
        }
        if (node.has("priority")) {
            request.setPriority(readRequiredPriority(parser, node.get("priority")));
        }
        if (node.has("assignee")) {
            request.setAssignee(readRequiredTextField(parser, node.get("assignee"), "Assignee"));
        }

        return request;
    }

    private Iterable<String> iterable(java.util.Iterator<String> iterator) {
        return () -> iterator;
    }

    private Optional<String> readRequiredTextField(JsonParser parser, JsonNode node, String fieldName)
            throws JsonMappingException {
        if (node.isNull()) {
            throw JsonMappingException.from(parser, fieldName + " must not be null.");
        }
        if (!node.isTextual()) {
            throw JsonMappingException.from(parser, fieldName + " must be a string.");
        }
        return Optional.of(node.asText());
    }

    private Optional<TicketPriority> readRequiredPriority(JsonParser parser, JsonNode node)
            throws JsonMappingException {
        if (node.isNull()) {
            throw JsonMappingException.from(parser, "Priority must not be null.");
        }
        if (!node.isTextual()) {
            throw JsonMappingException.from(parser, "Priority must be a string.");
        }

        try {
            return Optional.of(TicketPriority.valueOf(node.asText()));
        } catch (IllegalArgumentException exception) {
            throw JsonMappingException.from(parser, "Priority must be one of LOW, MEDIUM, HIGH, CRITICAL");
        }
    }
}
