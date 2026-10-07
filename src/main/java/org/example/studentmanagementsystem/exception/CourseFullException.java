package org.example.studentmanagementsystem.exception;

import org.springframework.http.HttpStatus;

/** No free seat left in the course. Handled by GlobalExceptionHandler as 409. */
public class CourseFullException extends BusinessException {

    public CourseFullException(String message) {
        super(message, "COURSE_FULL", HttpStatus.CONFLICT);
    }
}