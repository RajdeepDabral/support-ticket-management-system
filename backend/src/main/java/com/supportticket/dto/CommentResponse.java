package com.supportticket.dto;

import java.time.OffsetDateTime;

public class CommentResponse {

    private final Long id;
    private final String content;
    private final String author;
    private final OffsetDateTime createdAt;

    public CommentResponse(Long id, String content, String author, OffsetDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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
