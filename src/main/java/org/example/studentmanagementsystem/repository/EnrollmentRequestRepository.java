package org.example.studentmanagementsystem.repository;

import jakarta.persistence.LockModeType;
import org.example.studentmanagementsystem.entity.EnrollmentRequest;
import org.example.studentmanagementsystem.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, Integer> {

    boolean existsByStudentIdAndCourseIdAndStatus(Integer studentId, Integer courseId, EnrollmentStatus status);

    List<EnrollmentRequest> findByStatusOrderByIdDesc(EnrollmentStatus status);

    List<EnrollmentRequest> findByStudentIdOrderByIdDesc(Integer studentId);

    void deleteByStudentId(Integer studentId);

    void deleteByCourseId(Integer courseId);

    // Locks the request row so a double-click on "approve" cannot process it twice.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from EnrollmentRequest r where r.id = :id")
    Optional<EnrollmentRequest> findByIdForUpdate(@Param("id") Integer id);
}