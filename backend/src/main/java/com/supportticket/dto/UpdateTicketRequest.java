package com.supportticket.dto;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.supportticket.domain.TicketPriority;
import com.supportticket.exception.InvalidRequestException;

@JsonDeserialize(using = UpdateTicketRequestDeserializer.class)
public class UpdateTicketRequest {

    private Optional<String> title = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<TicketPriority> priority = Optional.empty();
    private Optional<String> assignee = Optional.empty();

    public Optional<String> getTitle() {
        return title;
    }

    void setTitle(Optional<String> title) {
        this.title = title;
    }

    public Optional<String> getDescription() {
        return description;
    }

    void setDescription(Optional<String> description) {
        this.description = description;
    }

    public Optional<TicketPriority> getPriority() {
        return priority;
    }

    void setPriority(Optional<TicketPriority> priority) {
        this.priority = priority;
    }

    public Optional<String> getAssignee() {
        return assignee;
    }

    void setAssignee(Optional<String> assignee) {
        this.assignee = assignee;
    }

    public boolean hasUpdates() {
        return title.isPresent() || description.isPresent() || priority.isPresent() || assignee.isPresent();
    }

    public UpdateTicketCommand toCommand() {
        if (!hasUpdates()) {
            throw new InvalidRequestException("At least one updatable field must be provided.");
        }

        UpdateTicketCommand.Builder builder = UpdateTicketCommand.builder();
        title.ifPresent(builder::title);
        description.ifPresent(builder::description);
        priority.ifPresent(builder::priority);
        assignee.ifPresent(builder::assignee);
        return builder.build();
    }
}
