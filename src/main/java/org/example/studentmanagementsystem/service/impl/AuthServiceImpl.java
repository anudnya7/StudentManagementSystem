package org.example.studentmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.config.CacheNames;
import org.example.studentmanagementsystem.dto.AuthResponse;
import org.example.studentmanagementsystem.dto.LoginRequest;
import org.example.studentmanagementsystem.dto.ProfileResponse;
import org.example.studentmanagementsystem.dto.RegisterRequest;
import org.example.studentmanagementsystem.dto.RegisterResponse;
import org.example.studentmanagementsystem.entity.Role;
import org.example.studentmanagementsystem.entity.Student;
import org.example.studentmanagementsystem.entity.User;
import org.example.studentmanagementsystem.exception.EmailAlreadyExistsException;
import org.example.studentmanagementsystem.exception.InvalidCredentialsException;
import org.example.studentmanagementsystem.exception.PhoneAlreadyExistsException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.exception.UsernameAlreadyExistsException;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.example.studentmanagementsystem.repository.UserRepository;
import org.example.studentmanagementsystem.security.JwtService;
import org.example.studentmanagementsystem.service.AuthService;
import org.example.studentmanagementsystem.service.FileStorageService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    @Override
    @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest r, MultipartFile photo) throws IOException {
        log.info("Registering student with username: {}", r.username());

        if (userRepository.existsByUsernameIgnoreCase(r.username())) {
            throw new UsernameAlreadyExistsException(r.username());
        }
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

        // 1. the login (always STUDENT: registration can never create an admin)
        User user = userRepository.save(User.builder()
                .username(r.username())
                .password(passwordEncoder.encode(r.password()))
                .role(Role.STUDENT)
                .build());

        // 2. the student record linked to that login
        Student saved = studentRepository.save(Student.builder()
                .firstName(r.firstName())
                .lastName(r.lastName())
                .email(r.email())
                .phone(r.phone())
                .dateOfBirth(r.dateOfBirth())
                .user(user)
                .build());

        if (hasPhoto) {
            saved.setImagePath(fileStorageService.store(saved.getId(), photo));
        }

        log.info("Student registered with id: {}", saved.getId());
        return new RegisterResponse(saved.getId(), user.getUsername(), saved.getEmail(), saved.getImagePath());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        log.debug("Login attempt");
        User user = userRepository.findByUsernameIgnoreCase(r.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(r.password(), user.getPassword())) {
            log.warn("Failed login for user id: {}", user.getId());
            throw new InvalidCredentialsException();
        }

        log.info("User {} logged in", user.getId());
        return new AuthResponse(jwtService.generateToken(user.getUsername()), "Bearer", jwtService.getExpirationMs());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String role = user.getRole().name();

        // an admin has a login but no student record
        return studentRepository.findByUserUsernameIgnoreCase(username)
                .map(s -> new ProfileResponse(s.getId(), user.getUsername(), s.getFirstName(), s.getLastName(),
                        s.getEmail(), s.getPhone(), s.getDateOfBirth(), s.getImagePath() != null, role))
                .orElseGet(() -> new ProfileResponse(null, user.getUsername(), null, null,
                        null, null, null, false, role));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updatePhoto(String username, MultipartFile file) throws IOException {
        Student s = findStudent(username);
        s.setImagePath(fileStorageService.store(s.getId(), file));
        return s.getImagePath();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getPhoto(String username) {
        return fileStorageService.load(findStudent(username).getImagePath());
    }

    private Student findStudent(String username) {
        return studentRepository.findByUserUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }
}