package org.example.studentmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        @NotBlank(message = "Department name is required")
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 10, message = "Code must be at most 10 characters")
        String code
) {
}