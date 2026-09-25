package com.supportticket.mapper;

import com.supportticket.domain.Comment;
import com.supportticket.dto.CommentResponse;

import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getContent(),
                comment.getAuthor(),
                comment.getCreatedAt());
    }
}
