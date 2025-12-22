package com.ratana.prodstack.controller;

import com.ratana.prodstack.dto.BlogPostResponse;
import com.ratana.prodstack.dto.CommentRequest;
import com.ratana.prodstack.dto.CommentResponse;
import com.ratana.prodstack.dto.MessageResponse;
import com.ratana.prodstack.service.BlogPostService;
import com.ratana.prodstack.service.CommentService;
import com.ratana.prodstack.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BlogPostController {

    private final BlogPostService blogPostService;
    private final LikeService likeService;
    private final CommentService commentService;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlogPostResponse> createBlogPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            BlogPostResponse response = blogPostService.createBlogPost(title, content, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<BlogPostResponse>> getAllBlogPosts() {
        List<BlogPostResponse> posts = blogPostService.getAllBlogPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponse> getBlogPostById(@PathVariable Long id) {
        try {
            BlogPostResponse response = blogPostService.getBlogPostById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlogPostResponse> updateBlogPost(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        try {
            BlogPostResponse response = blogPostService.updateBlogPost(id, title, content, images);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteBlogPost(@PathVariable Long id) {
        try {
            blogPostService.deleteBlogPost(id);
            return ResponseEntity.ok(new MessageResponse("Blog post deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteImage(@PathVariable Long imageId) {
        try {
            blogPostService.deleteImage(imageId);
            return ResponseEntity.ok(new MessageResponse("Image deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Like endpoints
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> toggleLike(@PathVariable Long id) {
        try {
            likeService.toggleLike(id);
            return ResponseEntity.ok(new MessageResponse("Like toggled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Comment endpoints
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        List<CommentResponse> comments = commentService.getComments(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request) {
        try {
            CommentResponse response = commentService.addComment(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}