package org.example.studentmanagementsystem.dto;

import java.time.Instant;

public record NotificationResponse(
        Integer id,
        String recipientEmail,
        String message,
        Instant createdAt
) {
}