package org.example.studentmanagementsystem.security;

import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.entity.User;
import org.example.studentmanagementsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + username));

        return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRole().name())   // ADMIN -> ROLE_ADMIN, STUDENT -> ROLE_STUDENT
                .build();
    }
}