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

    // the student that belongs to a login (empty for an admin)
    Optional<Student> findByUserUsernameIgnoreCase(String username);

    boolean existsByDepartmentId(Integer departmentId);

    boolean existsByCoursesId(Integer courseId);
}