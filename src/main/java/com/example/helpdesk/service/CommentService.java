package com.example.helpdesk.service;

import com.example.helpdesk.model.Comment;
import com.example.helpdesk.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getCommentsByTicketId(Long ticketId) {
        return commentRepository.findByTicketId(ticketId);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }
}
