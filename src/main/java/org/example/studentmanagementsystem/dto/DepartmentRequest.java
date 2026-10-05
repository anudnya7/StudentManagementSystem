package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        @Schema(example = "Computer Science")
        @NotBlank(message = "Department name is required")
        String name,

        @Schema(example = "CSE", description = "Unique, max 10 characters")
        @NotBlank(message = "Department code is required")
        @Size(max = 10, message = "Code must be at most 10 characters")
        String code
) {
}