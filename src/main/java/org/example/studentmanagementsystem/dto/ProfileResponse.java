package org.example.studentmanagementsystem.dto;

import java.time.LocalDate;

public record ProfileResponse(
        Integer id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        boolean hasPhoto
) {
}