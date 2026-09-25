package com.supportticket.dto;

import java.time.OffsetDateTime;

public class CommentResponse {

    private final Long id;
    private final Long ticketId;
    private final String content;
    private final String author;
    private final OffsetDateTime createdAt;

    public CommentResponse(Long id, Long ticketId, String content, String author, OffsetDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
