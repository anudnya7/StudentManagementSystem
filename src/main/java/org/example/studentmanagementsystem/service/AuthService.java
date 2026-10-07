package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.AuthResponse;
import org.example.studentmanagementsystem.dto.LoginRequest;
import org.example.studentmanagementsystem.dto.ProfileResponse;
import org.example.studentmanagementsystem.dto.RegisterRequest;
import org.example.studentmanagementsystem.dto.RegisterResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AuthService {

    RegisterResponse register(RegisterRequest request, MultipartFile photo) throws IOException;

    AuthResponse login(LoginRequest request);

    ProfileResponse getProfile(String email);

    String updatePhoto(String email, MultipartFile file) throws IOException;

    Resource getPhoto(String email);
}