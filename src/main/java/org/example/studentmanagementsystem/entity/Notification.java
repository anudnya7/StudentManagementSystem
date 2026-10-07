package org.example.studentmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Deliberately simple: a row here is the proof that "the student was told".
 * No real email is sent. It is saved in the SAME transaction as the approval,
 * so a notification can never exist for an approval that was rolled back.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false, length = 255)
    private String message;
}