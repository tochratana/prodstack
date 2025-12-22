package com.ratana.prodstack.repository;

import com.ratana.prodstack.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndBlogPostId(Long userId, Long blogPostId);
    boolean existsByUserIdAndBlogPostId(Long userId, Long blogPostId);
    int countByBlogPostId(Long blogPostId);
}

