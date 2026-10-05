package org.example.studentmanagementsystem.dto;

public record CourseResponse(
        Integer id,
        String title,
        String code,
        int credits,
        Integer departmentId,
        String departmentName
) {
}