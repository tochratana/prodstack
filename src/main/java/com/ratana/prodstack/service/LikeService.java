package com.ratana.prodstack.service;


import com.ratana.prodstack.model.BlogPost;
import com.ratana.prodstack.model.Like;
import com.ratana.prodstack.model.User;
import com.ratana.prodstack.repository.BlogPostRepository;
import com.ratana.prodstack.repository.LikeRepository;
import com.ratana.prodstack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleLike(Long postId) {
        User currentUser = getCurrentUser();
        BlogPost blogPost = blogPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Blog post not found"));

        var existingLike = likeRepository.findByUserIdAndBlogPostId(currentUser.getId(), postId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            Like like = new Like();
            like.setUser(currentUser);
            like.setBlogPost(blogPost);
            likeRepository.save(like);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
