package com.supportticket.dto;

import com.supportticket.domain.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTicketRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotBlank(message = "Assignee must not be blank")
    @Size(max = 255, message = "Assignee must not exceed 255 characters")
    private String assignee;

    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String title, String description, TicketPriority priority, String assignee) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignee = assignee;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
