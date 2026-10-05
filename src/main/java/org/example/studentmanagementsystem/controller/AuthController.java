package org.example.studentmanagementsystem.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.dto.AuthResponse;
import org.example.studentmanagementsystem.dto.LoginRequest;
import org.example.studentmanagementsystem.dto.RegisterRequest;
import org.example.studentmanagementsystem.dto.RegisterResponse;
import org.example.studentmanagementsystem.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Authentication", description = "Register and log in (public, no token needed)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a student", description = "multipart/form-data with a JSON part named 'data' and an optional image part named 'photo' (JPEG or PNG, max 2MB).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid image"),
            @ApiResponse(responseCode = "409", description = "Email or phone already exists"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestPart("data") RegisterRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        return new ResponseEntity<>(authService.register(data, photo), HttpStatus.CREATED);
    }

    @Operation(summary = "Log in", description = "Returns a JWT. Click Authorize in Swagger and paste the token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token issued"),
            @ApiResponse(responseCode = "401", description = "Wrong email or password")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}