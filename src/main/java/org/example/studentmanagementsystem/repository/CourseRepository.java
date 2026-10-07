package org.example.studentmanagementsystem.repository;

import jakarta.persistence.LockModeType;
import org.example.studentmanagementsystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByDepartmentId(Integer departmentId);

    List<Course> findByDepartmentId(Integer departmentId);

    // SELECT ... FOR UPDATE: other transactions wait here until this one commits.
    // Used before changing enrolledCount so two requests cannot take the last seat together.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdForUpdate(@Param("id") Integer id);
}