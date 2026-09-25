package com.supportticket.service;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.dto.CommentResponse;
import com.supportticket.dto.CreateCommentRequest;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.mapper.CommentMapper;
import com.supportticket.repository.CommentRepository;
import com.supportticket.repository.TicketRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            CommentMapper commentMapper) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Comment comment = new Comment(
                ticket,
                request.getContent().trim(),
                request.getAuthor().trim());

        return commentMapper.toResponse(commentRepository.save(comment));
    }
}
