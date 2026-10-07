package org.example.studentmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.config.OpenApiConfig;
import org.example.studentmanagementsystem.dto.NotificationResponse;
import org.example.studentmanagementsystem.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notifications", description = "Messages saved when a request is approved or rejected")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List notifications", description = "Newest first. Optionally only for one email.")
    @GetMapping
    public List<NotificationResponse> getAll(
            @Parameter(description = "Student email") @RequestParam(required = false) String email) {
        return email == null ? notificationService.getAll() : notificationService.getByEmail(email);
    }
}