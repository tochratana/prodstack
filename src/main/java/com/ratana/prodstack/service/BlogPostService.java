package com.ratana.prodstack.service;


import com.ratana.prodstack.dto.BlogPostResponse;
import com.ratana.prodstack.model.BlogImage;
import com.ratana.prodstack.model.BlogPost;
import com.ratana.prodstack.model.User;
import com.ratana.prodstack.repository.BlogImageRepository;
import com.ratana.prodstack.repository.BlogPostRepository;
import com.ratana.prodstack.repository.LikeRepository;
import com.ratana.prodstack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final BlogImageRepository blogImageRepository;
    private final LikeRepository likeRepository;
    private final FileStorageService fileStorageService;

    @Value("${file.blog-dir}")
    private String blogDir;

    @Transactional
    public BlogPostResponse createBlogPost(String title, String content, List<MultipartFile> images) throws IOException {
        User currentUser = getCurrentUser();

        BlogPost blogPost = new BlogPost();
        blogPost.setTitle(title);
        blogPost.setContent(content);
        blogPost.setAuthor(currentUser);
        blogPost.setImages(new ArrayList<>());

        BlogPost savedPost = blogPostRepository.save(blogPost);

        // Save images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String filename = fileStorageService.storeBlogImage(image);
                BlogImage blogImage = new BlogImage();
                blogImage.setFilename(filename);
                blogImage.setFilepath("/api/files/blog/" + filename);
                blogImage.setBlogPost(savedPost);
                blogImageRepository.save(blogImage);
            }
        }

        return mapToResponse(blogPostRepository.findById(savedPost.getId()).orElseThrow(), currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<BlogPostResponse> getAllBlogPosts() {
        User currentUser = getCurrentUserOrNull();
        Long userId = currentUser != null ? currentUser.getId() : null;

        return blogPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> mapToResponse(post, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getBlogPostById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        User currentUser = getCurrentUserOrNull();
        Long userId = currentUser != null ? currentUser.getId() : null;

        return mapToResponse(blogPost, userId);
    }

    @Transactional
    public BlogPostResponse updateBlogPost(Long id, String title, String content, List<MultipartFile> newImages) throws IOException {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();

        if (!blogPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this blog post");
        }

        blogPost.setTitle(title);
        blogPost.setContent(content);

        // Add new images
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                String filename = fileStorageService.storeBlogImage(image);
                BlogImage blogImage = new BlogImage();
                blogImage.setFilename(filename);
                blogImage.setFilepath("/api/files/blog/" + filename);
                blogImage.setBlogPost(blogPost);
                blogImageRepository.save(blogImage);
            }
        }

        BlogPost updatedPost = blogPostRepository.save(blogPost);

        return mapToResponse(updatedPost, currentUser.getId());
    }

    @Transactional
    public void deleteBlogPost(Long id) throws IOException {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        User currentUser = getCurrentUser();

        if (!blogPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this blog post");
        }

        // Delete images from storage
        List<BlogImage> images = blogImageRepository.findByBlogPostId(id);
        for (BlogImage image : images) {
            fileStorageService.deleteFile(image.getFilename(), blogDir);
        }

        blogPostRepository.delete(blogPost);
    }

    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        BlogImage image = blogImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        User currentUser = getCurrentUser();
        if (!image.getBlogPost().getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this image");
        }

        fileStorageService.deleteFile(image.getFilename(), blogDir);
        blogImageRepository.delete(image);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private User getCurrentUserOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return null;
            }
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private BlogPostResponse mapToResponse(BlogPost blogPost, Long currentUserId) {
        BlogPostResponse response = new BlogPostResponse();
        response.setId(blogPost.getId());
        response.setTitle(blogPost.getTitle());
        response.setContent(blogPost.getContent());
        response.setAuthorUsername(blogPost.getAuthor().getUsername());
        response.setAuthorProfileImage(blogPost.getAuthor().getProfileImage());
        response.setCreatedAt(blogPost.getCreatedAt());
        response.setUpdatedAt(blogPost.getUpdatedAt());

        // Get images
        List<BlogImage> images = blogImageRepository.findByBlogPostId(blogPost.getId());
        List<String> imagePaths = images.stream()
                .map(BlogImage::getFilepath)
                .collect(Collectors.toList());
        response.setImages(imagePaths);

        // Get like count and check if current user liked
        response.setLikeCount(likeRepository.countByBlogPostId(blogPost.getId()));
        if (currentUserId != null) {
            response.setLikedByCurrentUser(likeRepository.existsByUserIdAndBlogPostId(currentUserId, blogPost.getId()));
        } else {
            response.setLikedByCurrentUser(false);
        }

        return response;
    }
}