package org.example.studentmanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EnrollmentApplyRequest(

        @Schema(example = "1", description = "Id of an existing student")
        @NotNull(message = "studentId is required")
        Integer studentId,

        @Schema(example = "1", description = "Id of an existing course")
        @NotNull(message = "courseId is required")
        Integer courseId
) {
}