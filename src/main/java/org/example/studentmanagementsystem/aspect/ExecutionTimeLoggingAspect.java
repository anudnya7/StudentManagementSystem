package org.example.studentmanagementsystem.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting concern: how long every service method takes, logged without a single line
 * of timing code inside the services. One class adds timing to all of them.
 *
 * Pointcut: org.example.studentmanagementsystem.service.*.*(..)
 * That is one level under "service", i.e. the service INTERFACES (StudentService, CourseService,
 * DepartmentService, EnrollmentRequestService, NotificationService, FileStorageService).
 * Calls to their implementations in service.impl are matched through the interface.
 *
 * AuthService is excluded on purpose: its arguments are RegisterRequest / LoginRequest
 * and would print the plain-text password into the log file.
 *
 * Spring Boot switches AOP proxying on by itself once the aspectj starter is on the classpath,
 * so no @EnableAspectJAutoProxy is needed.
 */
@Slf4j
@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    @Around("execution(* org.example.studentmanagementsystem.service.*.*(..))"
            + " && !execution(* org.example.studentmanagementsystem.service.AuthService.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("[AOP] {} args={} executed in {} ms",
                    signature, Arrays.toString(joinPoint.getArgs()), System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("[AOP] {} threw {} after {} ms",
                    signature, ex.getClass().getSimpleName(), System.currentTimeMillis() - start);
            // Re-throw: the aspect only OBSERVES the call. GlobalExceptionHandler still turns the
            // exception into the HTTP response exactly as before.
            throw ex;
        }
    }
}