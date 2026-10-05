package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

public class PhoneAlreadyExistsException extends BusinessException {

    public PhoneAlreadyExistsException(String phone) {
        super("Phone already registered: " + phone, "PHONE_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}