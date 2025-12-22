package com.ratana.prodstack.repository;


import com.ratana.prodstack.model.BlogImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogImageRepository extends JpaRepository<BlogImage, Long> {
    List<BlogImage> findByBlogPostId(Long blogPostId);
}
