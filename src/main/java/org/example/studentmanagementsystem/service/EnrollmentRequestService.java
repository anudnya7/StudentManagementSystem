package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.EnrollmentApplyRequest;
import org.example.studentmanagementsystem.dto.EnrollmentResponse;
import org.example.studentmanagementsystem.entity.EnrollmentStatus;

import java.util.List;

public interface EnrollmentRequestService {

    EnrollmentResponse apply(EnrollmentApplyRequest request);

    EnrollmentResponse getById(int id);

    List<EnrollmentResponse> getAll(EnrollmentStatus status);

    List<EnrollmentResponse> getByStudent(int studentId);

    EnrollmentResponse approve(int id);

    EnrollmentResponse reject(int id);
}