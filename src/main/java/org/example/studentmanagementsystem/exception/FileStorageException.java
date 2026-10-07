package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

/** The file could not be written to disk (a server problem, not a client mistake). */
public class FileStorageException extends BusinessException {

    public FileStorageException(String message, Throwable cause) {
        super(message, "FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}