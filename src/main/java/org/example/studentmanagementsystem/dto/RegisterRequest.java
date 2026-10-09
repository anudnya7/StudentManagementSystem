package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record RegisterRequest(

        @Schema(example = "rahul", description = "3 to 30 characters: letters, digits, dot, underscore, dash")
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be 3 to 30 characters")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username may contain only letters, digits, dot, underscore and dash")
        String username,

        @Schema(example = "Rahul")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(example = "Sharma")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(example = "rahul@college.edu")
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @Schema(example = "9876543210", description = "Exactly 10 digits")
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
        String phone,

        @Schema(example = "2004-05-14", description = "Format yyyy-MM-dd")
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateOfBirth,

        @Schema(example = "StrongPass123", description = "8 to 64 characters")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters")
        String password
) {
}