package org.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true, length = 15)
    private String code;

    private int credits;

    // Maximum seats. columnDefinition default lets Hibernate add this NOT NULL column to a table that already has rows.
    @Column(nullable = false, columnDefinition = "integer default 30")
    @Builder.Default
    private Integer capacity = 30;

    // Seats taken. Only changed inside services (approve / enroll / drop), never set by a client.
    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer enrolledCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(mappedBy = "courses")
    @Builder.Default
    private Set<Student> students = new HashSet<>();
}