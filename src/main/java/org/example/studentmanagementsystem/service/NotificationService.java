package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getAll();

    List<NotificationResponse> getByEmail(String email);
}