package org.example.studentmanagementsystem.dto;

import org.example.studentmanagementsystem.entity.EnrollmentStatus;

import java.time.Instant;

public record EnrollmentResponse(
        Integer id,
        Integer studentId,
        String studentName,
        Integer courseId,
        String courseTitle,
        EnrollmentStatus status,
        Instant requestedAt,
        String requestedBy,
        Instant updatedAt,
        String updatedBy
) {
}