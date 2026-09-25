package com.supportticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotBlank(message = "Content must not be blank")
    private String content;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(String content, String author) {
        this.content = content;
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
