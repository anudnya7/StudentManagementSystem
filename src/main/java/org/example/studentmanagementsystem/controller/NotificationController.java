package org.example.studentmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.config.OpenApiConfig;
import org.example.studentmanagementsystem.dto.NotificationResponse;
import org.example.studentmanagementsystem.service.AuthService;
import org.example.studentmanagementsystem.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final AuthService authService;

    @Operation(summary = "List all notifications (ADMIN)", description = "Newest first. Optionally only for one email.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<NotificationResponse> getAll(
            @Parameter(description = "Student email") @RequestParam(required = false) String email) {
        return email == null ? notificationService.getAll() : notificationService.getByEmail(email);
    }

    // A student reads only his own messages. The username comes from the login, not from the URL,
    // so nobody can ask for somebody else's notifications. An admin has no student email: empty list.
    @Operation(summary = "My notifications", description = "Only the messages sent to the logged-in user, newest first.")
    @GetMapping("/my")
    public List<NotificationResponse> myNotifications(Authentication authentication) {
        String email = authService.getProfile(authentication.getName()).email();
        return email == null ? List.of() : notificationService.getByEmail(email);
    }
}