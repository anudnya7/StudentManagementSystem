package org.example.studentmanagementsystem.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.exception.FileStorageException;
import org.example.studentmanagementsystem.exception.InvalidFileException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final List<String> EXTENSIONS = List.of(".jpg", ".png");

    private final Path root;

    public FileStorageServiceImpl(@Value("${app.upload-dir}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory: " + dir, ex);
        }
        log.info("Upload directory: {}", root);
    }

    /** Checks the file is a real JPEG or PNG (by its first bytes, not the client's claim). */
    @Override
    public String validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        try (InputStream in = file.getInputStream()) {
            byte[] h = in.readNBytes(4);
            if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
                return ".jpg";
            }
            if (h.length >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47) {
                return ".png";
            }
        }
        throw new InvalidFileException("Only JPEG and PNG images are allowed");
    }

    /** Saves the photo. The name comes from the student id, never from the client's filename. */
    @Override
    public String store(int studentId, MultipartFile file) throws IOException {
        String extension = validate(file);
        deleteExisting(studentId);
        Path target = root.resolve("student-" + studentId + extension);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Stored photo for student {}", studentId);
        return target.getFileName().toString();
    }

    @Override
    public Resource load(String fileName) {
        if (fileName == null) {
            throw new ResourceNotFoundException("No photo uploaded");
        }
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new ResourceNotFoundException("Photo file not found");
        }
        return new FileSystemResource(path);
    }

    private void deleteExisting(int studentId) throws IOException {
        for (String ext : EXTENSIONS) {
            Files.deleteIfExists(root.resolve("student-" + studentId + ext));
        }
    }
}