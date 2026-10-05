package org.example.studentmanagementsystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logs one line per request and gives every request an id that appears on all its log lines.
 * It runs FIRST (highest precedence), before Spring Security, so requests that Security rejects
 * (401) are logged too and "Rejected invalid JWT" lines carry the request id.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);   // the client can quote it when reporting a problem
        long start = System.currentTimeMillis();
        log.trace("Request started: {} {}", request.getMethod(), request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (uri.startsWith("/actuator")) {
                log.debug("{} {} -> {} ({} ms)", method, uri, status, ms);   // health checks poll often: keep INFO quiet
            } else if (status >= 500) {
                log.error("{} {} -> {} ({} ms)", method, uri, status, ms);
            } else if (status >= 400) {
                log.warn("{} {} -> {} ({} ms)", method, uri, status, ms);
            } else {
                log.info("{} {} -> {} ({} ms)", method, uri, status, ms);
            }
            MDC.clear();
        }
    }
}