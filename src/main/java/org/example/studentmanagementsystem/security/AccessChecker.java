package org.example.studentmanagementsystem.security;

import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.entity.Student;
import org.example.studentmanagementsystem.repository.EnrollmentRequestRepository;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("accessChecker")
@RequiredArgsConstructor
public class AccessChecker {

    private final StudentRepository studentRepository;
    private final EnrollmentRequestRepository enrollmentRequestRepository;

    /** True when the logged-in user IS the student with this id. */
    @Transactional(readOnly = true)
    public boolean isSelf(int studentId, Authentication authentication) {
        return studentRepository.findById(studentId)
                .map(s -> belongsTo(s, authentication))
                .orElse(false);
    }

    /** True when the enrollment request belongs to the logged-in student. */
    @Transactional(readOnly = true)
    public boolean ownsRequest(int requestId, Authentication authentication) {
        return enrollmentRequestRepository.findById(requestId)
                .map(r -> belongsTo(r.getStudent(), authentication))
                .orElse(false);
    }

    private boolean belongsTo(Student student, Authentication authentication) {
        return student.getUser() != null
                && student.getUser().getUsername().equalsIgnoreCase(authentication.getName());
    }
}