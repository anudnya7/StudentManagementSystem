package org.example.studentmanagementsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    public static final String BODY =
            "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to do this\"}";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(BODY);
    }
}