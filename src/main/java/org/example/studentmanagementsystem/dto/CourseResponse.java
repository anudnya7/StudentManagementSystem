package org.example.studentmanagementsystem.dto;

import java.io.Serializable;
import java.time.Instant;

public record CourseResponse(
        Integer id,
        String title,
        String code,
        int credits,
        Integer departmentId,
        String departmentName,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) implements Serializable {
}
