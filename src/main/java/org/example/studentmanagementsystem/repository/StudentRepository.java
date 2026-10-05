package org.example.studentmanagementsystem.repository;

import org.example.studentmanagementsystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByDepartmentId(Integer departmentId);

    boolean existsByCoursesId(Integer courseId);
}