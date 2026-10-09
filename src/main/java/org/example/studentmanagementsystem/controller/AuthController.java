package org.example.studentmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.dto.AuthResponse;
import org.example.studentmanagementsystem.dto.LoginRequest;
import org.example.studentmanagementsystem.dto.RegisterRequest;
import org.example.studentmanagementsystem.dto.RegisterResponse;
import org.example.studentmanagementsystem.service.AuthService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Tag(name = "Authentication", description = "Register and log in (public, no token needed)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a student", description = "Form fields plus an optional photo (JPEG or PNG, max 2MB).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid image"),
            @ApiResponse(responseCode = "409", description = "Username, email or phone already exists"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @RequestParam
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 30, message = "Username must be 3 to 30 characters")
            @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username may contain only letters, digits, dot, underscore and dash")
            String username,

            @RequestParam @NotBlank(message = "First name is required") String firstName,

            @RequestParam @NotBlank(message = "Last name is required") String lastName,

            @RequestParam
            @NotBlank(message = "Email is required")
            @Email(message = "Email format is invalid")
            String email,

            @RequestParam
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
            String phone,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Past(message = "Date of birth must be in the past")
            LocalDate dateOfBirth,

            @RequestParam
            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters")
            String password,

            @RequestParam(value = "photo", required = false) MultipartFile photo) throws IOException {

        RegisterRequest request = new RegisterRequest(
                username, firstName, lastName, email, phone, dateOfBirth, password);
        return new ResponseEntity<>(authService.register(request, photo), HttpStatus.CREATED);
    }

    @Operation(summary = "Log in", description = "Returns a JWT. Click Authorize in Swagger and paste the token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token issued"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}