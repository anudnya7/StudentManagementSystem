package org.example.studentmanagementsystem.repository;

import org.example.studentmanagementsystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer>,
        JpaSpecificationExecutor<Student> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByDepartmentId(Integer departmentId);

    boolean existsByCoursesId(Integer courseId);
}