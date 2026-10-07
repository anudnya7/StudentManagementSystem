package org.example.studentmanagementsystem.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    /** Checks the file is a real JPEG or PNG (by its first bytes) and returns ".jpg" or ".png". */
    String validate(MultipartFile file) throws IOException;

    /** Saves the photo on disk and returns the stored file name to keep on the entity. */
    String store(int studentId, MultipartFile file) throws IOException;

    /** Loads a stored file by name. */
    Resource load(String fileName);
}