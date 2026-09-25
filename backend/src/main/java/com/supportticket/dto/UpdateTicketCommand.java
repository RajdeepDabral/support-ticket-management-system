package com.supportticket.dto;

import java.util.Optional;

import com.supportticket.domain.TicketPriority;

/**
 * Partial update command. Only present optional fields are applied.
 * Status is intentionally excluded; status changes use a dedicated operation.
 */
public class UpdateTicketCommand {

    private final Optional<String> title;
    private final Optional<String> description;
    private final Optional<TicketPriority> priority;
    private final Optional<String> assignee;

    private UpdateTicketCommand(
            Optional<String> title,
            Optional<String> description,
            Optional<TicketPriority> priority,
            Optional<String> assignee) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<TicketPriority> getPriority() {
        return priority;
    }

    public Optional<String> getAssignee() {
        return assignee;
    }

    public boolean hasUpdates() {
        return title.isPresent() || description.isPresent() || priority.isPresent() || assignee.isPresent();
    }

    public static final class Builder {

        private Optional<String> title = Optional.empty();
        private Optional<String> description = Optional.empty();
        private Optional<TicketPriority> priority = Optional.empty();
        private Optional<String> assignee = Optional.empty();

        public Builder title(String title) {
            if (title == null) {
                throw new IllegalArgumentException("Title cannot be explicitly null");
            }
            this.title = Optional.of(title);
            return this;
        }

        public Builder description(String description) {
            if (description == null) {
                throw new IllegalArgumentException("Description cannot be explicitly null");
            }
            this.description = Optional.of(description);
            return this;
        }

        public Builder priority(TicketPriority priority) {
            if (priority == null) {
                throw new IllegalArgumentException("Priority cannot be explicitly null");
            }
            this.priority = Optional.of(priority);
            return this;
        }

        public Builder assignee(String assignee) {
            if (assignee == null) {
                throw new IllegalArgumentException("Assignee cannot be explicitly null");
            }
            this.assignee = Optional.of(assignee);
            return this;
        }

        public UpdateTicketCommand build() {
            return new UpdateTicketCommand(title, description, priority, assignee);
        }
    }
}
