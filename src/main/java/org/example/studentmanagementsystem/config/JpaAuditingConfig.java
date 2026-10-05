package org.example.studentmanagementsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Switches auditing on. It lives in its own class (not on the main class) so that
 * slice tests such as @WebMvcTest do not need a JPA setup.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
}
