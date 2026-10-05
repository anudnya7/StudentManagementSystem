package org.example.studentmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.dto.CourseRequest;
import org.example.studentmanagementsystem.dto.CourseResponse;
import org.example.studentmanagementsystem.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return new ResponseEntity<>(courseService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public List<CourseResponse> getAll(@RequestParam(required = false) Integer departmentId) {
        return courseService.getAll(departmentId);
    }

    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable int id) {
        return courseService.getById(id);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable int id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        courseService.delete(id);
        return "Course deleted successfully";
    }
}