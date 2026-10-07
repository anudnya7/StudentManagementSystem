package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(

        @Schema(example = "Data Structures")
        @NotBlank(message = "Title is required")
        String title,

        @Schema(example = "CS201", description = "Unique, max 15 characters")
        @NotBlank(message = "Code is required")
        @Size(max = 15, message = "Code must be at most 15 characters")
        String code,

        @Schema(example = "4", description = "1 to 10")
        @NotNull(message = "Credits is required")
        @Min(value = 1, message = "Credits must be between 1 and 10")
        @Max(value = 10, message = "Credits must be between 1 and 10")
        Integer credits,

        @Schema(example = "1", description = "Id of an existing department")
        @NotNull(message = "departmentId is required")
        Integer departmentId,

        @Schema(example = "30", description = "Maximum seats, 1 to 500. Optional: defaults to 30 on create, unchanged on update")
        @Min(value = 1, message = "Capacity must be between 1 and 500")
        @Max(value = 500, message = "Capacity must be between 1 and 500")
        Integer capacity
) {
}