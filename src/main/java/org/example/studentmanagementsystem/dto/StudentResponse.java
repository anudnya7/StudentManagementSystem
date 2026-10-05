package org.example.studentmanagementsystem.dto;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse implements Serializable {
    private static final long serialVersionUID = 2L;

    private Integer id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private int age;
    private String departmentName;
    private List<String> courses;

    // audit info (from BaseEntity)
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}