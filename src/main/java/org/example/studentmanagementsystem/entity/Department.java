package org.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    // Bidirectional 1:N owned by Course (mappedBy = "department").
    // cascade = ALL: saving or deleting a Department cascades to its Courses.
    // orphanRemoval = true: a Course removed from THIS list is deleted from the database.
    // (DepartmentServiceImpl still refuses to delete a department that has courses or students.)
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}