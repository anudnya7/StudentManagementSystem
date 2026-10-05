package org.example.studentmanagementsystem.dto;

import java.io.Serializable;
import java.time.Instant;

public record DepartmentResponse(
        Integer id,
        String name,
        String code,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) implements Serializable {
}
