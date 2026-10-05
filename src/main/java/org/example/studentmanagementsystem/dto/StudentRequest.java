package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Data needed to create or update a student")
public record StudentRequest(

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
        LocalDate dateOfBirth
) {
}