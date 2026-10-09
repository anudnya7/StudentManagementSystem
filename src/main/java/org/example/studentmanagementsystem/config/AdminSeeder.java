package org.example.studentmanagementsystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.entity.Role;
import org.example.studentmanagementsystem.entity.User;
import org.example.studentmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsernameIgnoreCase(adminUsername)) {
            log.debug("Admin account {} already exists", adminUsername);
            return;
        }

        // an admin is only a login: no Student row is needed
        userRepository.save(User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build());
        log.info("Created admin account {}", adminUsername);
    }
}