package org.example.studentmanagementsystem.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Tells auditing WHO is making the change: the email stored in the JWT of the current request.
 * Requests without a login (registration, startup code) are recorded as "system".
 */
@Component("auditorProvider")
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    public static final String SYSTEM = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.of(SYSTEM);
        }
        return Optional.of(auth.getName());
    }
}
