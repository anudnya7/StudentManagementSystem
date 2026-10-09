package org.example.studentmanagementsystem.config;

import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.security.JwtAuthFilter;
import org.example.studentmanagementsystem.security.RestAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAccessDeniedHandler accessDeniedHandler;

    // ---------- chain 1: monitoring (HTTP Basic with its own user) ----------

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http,
                                                   @Value("${app.actuator.username}") String username,
                                                   @Value("${app.actuator.password}") String password,
                                                   PasswordEncoder encoder) throws Exception {
        var actuatorUsers = new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(encoder.encode(password))
                        .roles("ACTUATOR")
                        .build());
        var provider = new DaoAuthenticationProvider(actuatorUsers);
        provider.setPasswordEncoder(encoder);

        http
                .securityMatcher("/actuator/**")
                .authenticationProvider(provider)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().hasRole("ACTUATOR"))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // ---------- chain 2: the API (JWT) ----------

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // any logged-in user
                        .requestMatchers("/api/profile/**").authenticated()
                        // courses and departments: everybody logged in may read, only ADMIN may change
                        .requestMatchers(HttpMethod.GET, "/api/departments/**", "/api/courses/**")
                        .hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers("/api/departments/**", "/api/courses/**").hasRole("ADMIN")
                        // managing students is an ADMIN job
                        .requestMatchers("/api/students/**").hasRole("ADMIN")
                        // notifications: my own for everybody, the full list for ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/notifications/my").authenticated()
                        .requestMatchers("/api/notifications/**").hasRole("ADMIN")
                        // enrollment requests: finer rules are @PreAuthorize in the controller
                        .requestMatchers("/api/enrollment-requests/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))  // 401
                        .accessDeniedHandler(accessDeniedHandler))                                    // 403
                // reads "Authorization: Bearer <token>" before Spring's own login filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}