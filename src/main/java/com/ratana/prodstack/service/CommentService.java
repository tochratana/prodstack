package com.ratana.prodstack.service;


import com.ratana.prodstack.dto.CommentRequest;
import com.ratana.prodstack.dto.CommentResponse;
import com.ratana.prodstack.model.BlogPost;
import com.ratana.prodstack.model.Comment;
import com.ratana.prodstack.model.User;
import com.ratana.prodstack.repository.BlogPostRepository;
import com.ratana.prodstack.repository.CommentRepository;
import com.ratana.prodstack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request) {
        User currentUser = getCurrentUser();
        BlogPost blogPost = blogPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Blog post not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(currentUser);
        comment.setBlogPost(blogPost);

        Comment savedComment = commentRepository.save(comment);

        return mapToResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        return commentRepository.findByBlogPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User currentUser = getCurrentUser();

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUsername(comment.getUser().getUsername());
        response.setUserProfileImage(comment.getUser().getProfileImage());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}