package com.supportticket.mapper;

import java.util.List;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.dto.CommentResponse;
import com.supportticket.dto.TicketDetailResponse;
import com.supportticket.dto.TicketResponse;

import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    private final CommentMapper commentMapper;

    public TicketMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    public TicketDetailResponse toDetailResponse(Ticket ticket, List<Comment> comments) {
        List<CommentResponse> commentResponses = comments.stream()
                .map(commentMapper::toResponse)
                .toList();

        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                commentResponses);
    }
}
