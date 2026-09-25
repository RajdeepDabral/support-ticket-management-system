package com.supportticket.repository;

import java.util.List;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    @Query("""
            SELECT t FROM Ticket t
            WHERE (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR t.status = :status)
            """)
    List<Ticket> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") TicketStatus status);
}
