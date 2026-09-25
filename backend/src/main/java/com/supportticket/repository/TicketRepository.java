package com.supportticket.repository;

import java.util.List;

import com.supportticket.domain.Ticket;
import com.supportticket.domain.TicketStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:keyword IS NULL
                OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR t.status = :status)
            """)
    List<Ticket> search(
            @Param("keyword") String keyword,
            @Param("status") TicketStatus status);
}
