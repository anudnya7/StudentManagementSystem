package org.example.studentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.dto.CourseRequest;
import org.example.studentmanagementsystem.dto.CourseResponse;
import org.example.studentmanagementsystem.entity.Course;
import org.example.studentmanagementsystem.entity.Department;
import org.example.studentmanagementsystem.exception.BusinessException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.repository.CourseRepository;
import org.example.studentmanagementsystem.repository.DepartmentRepository;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    public CourseResponse create(CourseRequest r) {
        log.info("Creating course: {}", r.code());
        if (courseRepository.existsByCodeIgnoreCase(r.code())) {
            throw new BusinessException("Course code already exists: " + r.code(),
                    "COURSE_CODE_EXISTS", HttpStatus.CONFLICT);
        }
        Course saved = courseRepository.save(Course.builder()
                .title(r.title())
                .code(r.code().toUpperCase())
                .credits(r.credits())
                .department(findDepartment(r.departmentId()))
                .build());
        log.info("Course created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAll(Integer departmentId) {
        log.debug("Fetching courses, departmentId filter: {}", departmentId);
        List<Course> courses = departmentId == null
                ? courseRepository.findAll()
                : courseRepository.findByDepartmentId(departmentId);
        return courses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(int id) {
        log.debug("Fetching course with id: {}", id);
        return toResponse(find(id));
    }

    public CourseResponse update(int id, CourseRequest r) {
        log.info("Updating course with id: {}", id);
        Course c = find(id);

        if (!c.getCode().equalsIgnoreCase(r.code()) && courseRepository.existsByCodeIgnoreCase(r.code())) {
            throw new BusinessException("Course code already exists: " + r.code(),
                    "COURSE_CODE_EXISTS", HttpStatus.CONFLICT);
        }
        if (!c.getDepartment().getId().equals(r.departmentId()) && studentRepository.existsByCoursesId(id)) {
            throw new BusinessException("Students are enrolled, so the department cannot change",
                    "COURSE_HAS_STUDENTS", HttpStatus.CONFLICT);
        }

        c.setTitle(r.title());
        c.setCode(r.code().toUpperCase());
        c.setCredits(r.credits());
        c.setDepartment(findDepartment(r.departmentId()));
        return toResponse(courseRepository.save(c));
    }

    public void delete(int id) {
        log.info("Deleting course with id: {}", id);
        Course c = find(id);
        if (studentRepository.existsByCoursesId(id)) {
            throw new BusinessException("Students are enrolled in this course",
                    "COURSE_HAS_STUDENTS", HttpStatus.CONFLICT);
        }
        courseRepository.delete(c);
        log.info("Course deleted with id: {}", id);
    }

    private Course find(int id) {
        log.trace("Looking up course, id: {}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private Department findDepartment(int id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getTitle(), c.getCode(), c.getCredits(),
                c.getDepartment().getId(), c.getDepartment().getName());
    }
}