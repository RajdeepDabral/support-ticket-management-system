package com.supportticket.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;

public class TicketDetailResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final TicketPriority priority;
    private final TicketStatus status;
    private final String assignee;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<CommentResponse> comments;

    public TicketDetailResponse(
            Long id,
            String title,
            String description,
            TicketPriority priority,
            TicketStatus status,
            String assignee,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<CommentResponse> comments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignee = assignee;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getAssignee() {
        return assignee;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }
}
