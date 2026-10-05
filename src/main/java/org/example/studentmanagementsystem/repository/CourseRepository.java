package org.example.studentmanagementsystem.repository;

import org.example.studentmanagementsystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByDepartmentId(Integer departmentId);

    List<Course> findByDepartmentId(Integer departmentId);
}