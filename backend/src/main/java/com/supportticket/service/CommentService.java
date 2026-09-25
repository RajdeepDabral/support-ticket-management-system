package com.supportticket.service;

import com.supportticket.dto.CommentResponse;
import com.supportticket.dto.CreateCommentRequest;

public interface CommentService {

    CommentResponse addComment(Long ticketId, CreateCommentRequest request);
}
