package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends BusinessException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already taken: " + username, "USERNAME_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}