package com.ratana.prodstack.service;

import com.ratana.prodstack.dto.BlogPostRequest;
import com.ratana.prodstack.dto.BlogPostResponse;
import com.ratana.prodstack.model.BlogPost;
import com.ratana.prodstack.model.User;
import com.ratana.prodstack.repository.BlogPostRepository;
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
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public BlogPostResponse createBlogPost(BlogPostRequest request) {
        User currentUser = getCurrentUser();

        BlogPost blogPost = new BlogPost();
        blogPost.setTitle(request.getTitle());
        blogPost.setContent(request.getContent());
        blogPost.setAuthor(currentUser);

        BlogPost savedPost = blogPostRepository.save(blogPost);

        return mapToResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public List<BlogPostResponse> getAllBlogPosts() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getBlogPostById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        return mapToResponse(blogPost);
    }

    @Transactional
    public BlogPostResponse updateBlogPost(Long id, BlogPostRequest request) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();

        if (!blogPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this blog post");
        }

        blogPost.setTitle(request.getTitle());
        blogPost.setContent(request.getContent());

        BlogPost updatedPost = blogPostRepository.save(blogPost);

        return mapToResponse(updatedPost);
    }

    @Transactional
    public void deleteBlogPost(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();

        if (!blogPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this blog post");
        }

        blogPostRepository.delete(blogPost);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private BlogPostResponse mapToResponse(BlogPost blogPost) {
        return new BlogPostResponse(
                blogPost.getId(),
                blogPost.getTitle(),
                blogPost.getContent(),
                blogPost.getAuthor().getUsername(),
                blogPost.getCreatedAt(),
                blogPost.getUpdatedAt()
        );
    }
}