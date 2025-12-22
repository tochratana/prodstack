package com.ratana.prodstack.repository;


import com.ratana.prodstack.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBlogPostIdOrderByCreatedAtDesc(Long blogPostId);
}