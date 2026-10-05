package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "rahul@college.edu")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(example = "StrongPass123")
        @NotBlank(message = "Password is required")
        String password
) {
}