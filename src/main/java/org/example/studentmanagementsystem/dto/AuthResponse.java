package org.example.studentmanagementsystem.dto;

public record AuthResponse(String token, String type, long expiresInMs) {
}