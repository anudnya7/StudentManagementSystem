package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        // Same message for unknown email and wrong password, so attackers can't tell which
        super("Invalid email or password", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
    }
}