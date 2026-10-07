package org.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    // BCrypt hash. Null for students created without registration (cannot log in)
    private String password;

    // Name of the photo file on disk (the bytes live in the upload folder, not in the database).
    // The column keeps its old name photo_file_name so existing data still works.
    @Column(name = "photo_file_name", length = 255)
    private String imagePath;

    // @Embedded puts Address's columns straight into the students table.
    // "street" is renamed to address_line for this table only, without touching the Address class.
    @Embedded
    @AttributeOverride(name = "street", column = @Column(name = "address_line", length = 100))
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    @Builder.Default
    private Set<Course> courses = new HashSet<>();
}