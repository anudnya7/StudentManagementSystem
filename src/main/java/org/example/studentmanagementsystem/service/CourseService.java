package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.CourseRequest;
import org.example.studentmanagementsystem.dto.CourseResponse;

import java.util.List;

public interface CourseService {

    CourseResponse create(CourseRequest request);

    List<CourseResponse> getAll(Integer departmentId);

    CourseResponse getById(int id);

    CourseResponse update(int id, CourseRequest request);

    void delete(int id);
}