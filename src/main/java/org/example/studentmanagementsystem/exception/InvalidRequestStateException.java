package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

/** Someone tries to approve or reject an enrollment request that is no longer PENDING. */
public class InvalidRequestStateException extends BusinessException {

    public InvalidRequestStateException(String message) {
        super(message, "REQUEST_ALREADY_PROCESSED", HttpStatus.CONFLICT);
    }
}