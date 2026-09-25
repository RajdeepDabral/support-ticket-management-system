package com.supportticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.dto.CommentResponse;
import com.supportticket.dto.CreateCommentRequest;
import com.supportticket.exception.TicketNotFoundException;
import com.supportticket.mapper.CommentMapper;
import com.supportticket.repository.CommentRepository;
import com.supportticket.repository.TicketRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    private final CommentMapper commentMapper = new CommentMapper();

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(ticketRepository, commentRepository, commentMapper);
    }

    @Test
    void addComment_persistsCommentForExistingTicket() {
        Ticket ticket = persistedTicket(1L);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            ReflectionTestUtils.setField(comment, "id", 10L);
            ReflectionTestUtils.setField(comment, "createdAt", OffsetDateTime.parse("2026-09-25T11:30:00Z"));
            return comment;
        });

        CommentResponse response = commentService.addComment(
                1L,
                new CreateCommentRequest("I have investigated the issue.", "rajdeep"));

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTicketId()).isEqualTo(1L);
        assertThat(response.getContent()).isEqualTo("I have investigated the issue.");
        assertThat(response.getAuthor()).isEqualTo("rajdeep");
        assertThat(response.getCreatedAt()).isEqualTo(OffsetDateTime.parse("2026-09-25T11:30:00Z"));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_throwsWhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(
                        99L,
                        new CreateCommentRequest("Some comment", "rajdeep")))
                .isInstanceOf(TicketNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    private Ticket persistedTicket(Long id) {
        Ticket ticket = new Ticket(
                "Login issue",
                "User cannot login",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "rajdeep");
        ReflectionTestUtils.setField(ticket, "id", id);
        return ticket;
    }
}
