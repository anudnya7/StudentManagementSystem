package org.example.studentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.dto.*;
import org.example.studentmanagementsystem.entity.Student;
import org.example.studentmanagementsystem.exception.EmailAlreadyExistsException;
import org.example.studentmanagementsystem.exception.InvalidCredentialsException;
import org.example.studentmanagementsystem.exception.PhoneAlreadyExistsException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.example.studentmanagementsystem.security.JwtService;
import org.springframework.core.io.Resource;
import org.example.studentmanagementsystem.config.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest r, MultipartFile photo) throws IOException {
        log.info("Registering student with email: {}", r.email());

        if (studentRepository.existsByEmailIgnoreCase(r.email())) {
            throw new EmailAlreadyExistsException(r.email());
        }
        if (studentRepository.existsByPhone(r.phone())) {
            throw new PhoneAlreadyExistsException(r.phone());
        }

        boolean hasPhoto = photo != null && !photo.isEmpty();
        if (hasPhoto) {
            fileStorageService.validate(photo);   // reject a bad file before anything is saved
        }

        Student saved = studentRepository.save(Student.builder()
                .firstName(r.firstName())
                .lastName(r.lastName())
                .email(r.email())
                .phone(r.phone())
                .dateOfBirth(r.dateOfBirth())
                .password(passwordEncoder.encode(r.password()))
                .build());

        if (hasPhoto) {
            saved.setPhotoFileName(fileStorageService.store(saved.getId(), photo));
        }

        log.info("Student registered with id: {}", saved.getId());
        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getPhotoFileName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        log.debug("Login attempt");
        Student s = studentRepository.findByEmailIgnoreCase(r.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (s.getPassword() == null || !passwordEncoder.matches(r.password(), s.getPassword())) {
            log.warn("Failed login for student id: {}", s.getId());
            throw new InvalidCredentialsException();
        }

        log.info("Student {} logged in", s.getId());
        return new AuthResponse(jwtService.generateToken(s.getEmail()), "Bearer", jwtService.getExpirationMs());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        Student s = findByEmail(email);
        return new ProfileResponse(s.getId(), s.getFirstName(), s.getLastName(), s.getEmail(),
                s.getPhone(), s.getDateOfBirth(), s.getPhotoFileName() != null);
    }

    @Transactional(rollbackFor = Exception.class)
    public String updatePhoto(String email, MultipartFile file) throws IOException {
        Student s = findByEmail(email);
        s.setPhotoFileName(fileStorageService.store(s.getId(), file));
        return s.getPhotoFileName();
    }

    @Transactional(readOnly = true)
    public Resource getPhoto(String email) {
        return fileStorageService.load(findByEmail(email).getPhotoFileName());
    }

    private Student findByEmail(String email) {
        return studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }
}