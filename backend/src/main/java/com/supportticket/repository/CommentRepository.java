package com.supportticket.repository;

import java.util.List;

import com.supportticket.domain.Comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicket_Id(Long ticketId);
}
