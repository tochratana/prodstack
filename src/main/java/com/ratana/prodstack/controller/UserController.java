package com.ratana.prodstack.controller;

import com.ratana.prodstack.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("image") MultipartFile file) {
        try {
            String filepath = userService.uploadProfileImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("profileImage", filepath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}