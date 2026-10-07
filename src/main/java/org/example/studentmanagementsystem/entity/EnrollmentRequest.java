package org.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * "Student X asks to join course Y". Created as PENDING, later APPROVED or REJECTED.
 * createdAt / createdBy (from BaseEntity) tell us when and who applied,
 * updatedAt / updatedBy tell us when and who approved or rejected.
 */
@Entity
@Table(name = "enrollment_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;
}