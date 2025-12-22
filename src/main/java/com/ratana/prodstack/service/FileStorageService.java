package com.ratana.prodstack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-dir}")
    private String profileDir;

    @Value("${file.blog-dir}")
    private String blogDir;

    public String storeProfileImage(MultipartFile file) throws IOException {
        return storeFile(file, profileDir);
    }

    public String storeBlogImage(MultipartFile file) throws IOException {
        return storeFile(file, blogDir);
    }

    private String storeFile(MultipartFile file, String directory) throws IOException {
        // Create directories if they don't exist
        Path dirPath = Paths.get(directory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Store file
        Path targetLocation = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public void deleteFile(String filename, String directory) throws IOException {
        Path filePath = Paths.get(directory).resolve(filename);
        Files.deleteIfExists(filePath);
    }

    public Path loadFile(String filename, String directory) {
        return Paths.get(directory).resolve(filename);
    }
}