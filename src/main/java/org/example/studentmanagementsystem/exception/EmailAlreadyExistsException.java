package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email, "EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}