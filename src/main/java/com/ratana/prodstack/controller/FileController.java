package com.ratana.prodstack.controller;


import com.ratana.prodstack.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Value("${file.profile-dir}")
    private String profileDir;

    @Value("${file.blog-dir}")
    private String blogDir;

    @GetMapping("/profile/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        return getFile(filename, profileDir);
    }

    @GetMapping("/blog/{filename:.+}")
    public ResponseEntity<Resource> getBlogImage(@PathVariable String filename) {
        return getFile(filename, blogDir);
    }

    private ResponseEntity<Resource> getFile(String filename, String directory) {
        try {
            Path filePath = fileStorageService.loadFile(filename, directory);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}