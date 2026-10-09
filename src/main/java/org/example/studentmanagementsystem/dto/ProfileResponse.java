package org.example.studentmanagementsystem.dto;

import java.time.LocalDate;

/** For an ADMIN (a login without a student record) the student fields are null. */
public record ProfileResponse(
        Integer id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        boolean hasPhoto,
        String role
) {
}