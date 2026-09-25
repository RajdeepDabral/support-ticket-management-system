package com.supportticket.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.supportticket.domain.Comment;
import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketPriority;
import com.supportticket.domain.TicketStatus;
import com.supportticket.support.AbstractPostgreSQLContainerTest;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketCommentPersistenceTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndRetrievesTicket() {
        Ticket ticket = new Ticket(
                "Unable to login",
                "User cannot access the application.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "john.doe");

        Ticket saved = ticketRepository.save(ticket);
        entityManager.flush();
        entityManager.clear();

        Ticket retrieved = ticketRepository.findById(saved.getId()).orElseThrow();

        assertThat(retrieved.getTitle()).isEqualTo("Unable to login");
        assertThat(retrieved.getDescription()).isEqualTo("User cannot access the application.");
        assertThat(retrieved.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(retrieved.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(retrieved.getAssignee()).isEqualTo("john.doe");
        assertThat(retrieved.getCreatedAt()).isNotNull();
        assertThat(retrieved.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsCommentAgainstTicket() {
        Ticket ticket = ticketRepository.save(new Ticket(
                "API timeout",
                "Requests are timing out.",
                TicketPriority.MEDIUM,
                TicketStatus.IN_PROGRESS,
                "jane.doe"));

        Comment comment = new Comment(ticket, "Investigating the timeout issue.", "jane.doe");
        Comment saved = commentRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        Comment retrieved = commentRepository.findById(saved.getId()).orElseThrow();

        assertThat(retrieved.getContent()).isEqualTo("Investigating the timeout issue.");
        assertThat(retrieved.getAuthor()).isEqualTo("jane.doe");
        assertThat(retrieved.getTicket().getId()).isEqualTo(ticket.getId());
        assertThat(retrieved.getCreatedAt()).isNotNull();
    }

    @Test
    void ticketCommentRelationshipWorks() {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Payment failure",
                "Payment gateway returns an error.",
                TicketPriority.CRITICAL,
                TicketStatus.OPEN,
                "alex.smith"));

        commentRepository.save(new Comment(ticket, "First investigation note.", "alex.smith"));
        commentRepository.save(new Comment(ticket, "Escalated to payments team.", "ops.team"));
        entityManager.flush();
        entityManager.clear();

        List<Comment> comments = commentRepository.findByTicket_Id(ticket.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getContent)
                .containsExactlyInAnyOrder("First investigation note.", "Escalated to payments team.");
    }

    @Test
    void statusPersistedAsString() {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Status check",
                "Verify enum storage.",
                TicketPriority.LOW,
                TicketStatus.RESOLVED,
                "reviewer"));

        entityManager.flush();

        String status = (String) entityManager.createNativeQuery(
                        "SELECT status FROM ticket WHERE id = :id")
                .setParameter("id", ticket.getId())
                .getSingleResult();

        assertThat(status).isEqualTo("RESOLVED");
    }

    @Test
    void priorityPersistedAsString() {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Priority check",
                "Verify enum storage.",
                TicketPriority.CRITICAL,
                TicketStatus.OPEN,
                "reviewer"));

        entityManager.flush();

        String priority = (String) entityManager.createNativeQuery(
                        "SELECT priority FROM ticket WHERE id = :id")
                .setParameter("id", ticket.getId())
                .getSingleResult();

        assertThat(priority).isEqualTo("CRITICAL");
    }

    @Test
    void rejectsTicketWithoutRequiredTitle() {
        Ticket ticket = new Ticket(
                null,
                "Missing title should fail.",
                TicketPriority.MEDIUM,
                TicketStatus.OPEN,
                "john.doe");

        assertThatThrownBy(() -> {
            ticketRepository.saveAndFlush(ticket);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsCommentWithoutTicket() {
        Comment orphanComment = new Comment(null, "Orphan comment.", "john.doe");

        assertThatThrownBy(() -> {
            commentRepository.saveAndFlush(orphanComment);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void dataSurvivesRepositoryRoundTrip() {
        Ticket ticket = ticketRepository.save(new Ticket(
                "Persistence round trip",
                "Data should survive save and reload.",
                TicketPriority.HIGH,
                TicketStatus.OPEN,
                "persist.test"));

        Long ticketId = ticket.getId();
        entityManager.flush();
        entityManager.clear();

        Ticket toUpdate = ticketRepository.findById(ticketId).orElseThrow();
        toUpdate.setTitle("Updated title");
        ticketRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        Ticket reloaded = ticketRepository.findById(ticketId).orElseThrow();

        assertThat(reloaded.getTitle()).isEqualTo("Updated title");
        assertThat(reloaded.getUpdatedAt()).isAfterOrEqualTo(reloaded.getCreatedAt());
    }
}
