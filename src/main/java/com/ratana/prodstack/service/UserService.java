package com.ratana.prodstack.service;

import com.ratana.prodstack.model.User;
import com.ratana.prodstack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${file.profile-dir}")
    private String profileDir;

    @Transactional
    public String uploadProfileImage(MultipartFile file) throws IOException {
        User currentUser = getCurrentUser();

        // Delete old profile image if exists
        if (currentUser.getProfileImage() != null) {
            try {
                String oldFilename = currentUser.getProfileImage().replace("/api/files/profile/", "");
                fileStorageService.deleteFile(oldFilename, profileDir);
            } catch (IOException e) {
                // Ignore if old file doesn't exist
            }
        }

        // Store new image
        String filename = fileStorageService.storeProfileImage(file);
        String filepath = "/api/files/profile/" + filename;

        currentUser.setProfileImage(filepath);
        userRepository.save(currentUser);

        return filepath;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
