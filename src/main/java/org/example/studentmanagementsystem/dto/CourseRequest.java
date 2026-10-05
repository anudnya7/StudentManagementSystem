package org.example.studentmanagementsystem.dto;

import jakarta.validation.constraints.*;

public record CourseRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Code is required")
        @Size(max = 15, message = "Code must be at most 15 characters")
        String code,

        @Min(value = 1, message = "Credits must be between 1 and 10")
        @Max(value = 10, message = "Credits must be between 1 and 10")
        int credits,

        @NotNull(message = "departmentId is required")
        Integer departmentId
) {
}