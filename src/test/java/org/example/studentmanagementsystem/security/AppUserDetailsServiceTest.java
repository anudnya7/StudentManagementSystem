package org.example.studentmanagementsystem.security;

import org.example.studentmanagementsystem.entity.Role;
import org.example.studentmanagementsystem.entity.User;
import org.example.studentmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppUserDetailsServiceTest {

    private final UserRepository repository = Mockito.mock(UserRepository.class);
    private final AppUserDetailsService service = new AppUserDetailsService(repository);

    @Test
    void adminGetsRoleAdmin() {
        User admin = User.builder().username("admin").password("hash").role(Role.ADMIN).build();
        Mockito.when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        UserDetails user = service.loadUserByUsername("admin");

        assertEquals("admin", user.getUsername());
        assertEquals("hash", user.getPassword());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void studentGetsRoleStudentByDefault() {
        User u = User.builder().username("rahul").password("hash").build();
        Mockito.when(repository.findByUsernameIgnoreCase("rahul")).thenReturn(Optional.of(u));

        UserDetails user = service.loadUserByUsername("rahul");

        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void unknownUsernameIsRejected() {
        Mockito.when(repository.findByUsernameIgnoreCase("nobody")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nobody"));
    }
}