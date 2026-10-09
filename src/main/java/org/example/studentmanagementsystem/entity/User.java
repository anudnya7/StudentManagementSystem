package org.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")   // "user" is a reserved word in PostgreSQL
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // BCrypt hash, never the plain password
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.STUDENT;
}