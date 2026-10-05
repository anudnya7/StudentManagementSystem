package org.example.studentmanagementsystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Duplicate email, duplicate phone, and any future business rule
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        log.warn("Business rule violated [{}]: {}", ex.getErrorCode(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("code", ex.getErrorCode());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Single object validation (@Valid on a request body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

//    // List validation (/createall)
//    @ExceptionHandler(HandlerMethodValidationException.class)
//    public ResponseEntity<Map<String, Object>> handleMethodValidation(HandlerMethodValidationException ex) {
//        Map<String, String> errors = new LinkedHashMap<>();
//        ex.getAllValidationResults().forEach(result ->
//                result.getResolvableErrors().forEach(err ->
//                        errors.put(result.getMethodParameter().getParameterName()
//                                        + "[" + result.getContainerIndex() + "]",
//                                err.getDefaultMessage())));
//
//        log.warn("Method validation failed: {}", errors);
//
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("timestamp", LocalDateTime.now());
//        body.put("status", 400);
//        body.put("error", "Validation Failed");
//        body.put("errors", errors);
//        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
//    }

    // Broken JSON, empty body, wrong date format
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", "Request body is missing or malformed (check JSON and date format yyyy-MM-dd)");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // e.g. /get/abc where an int id is expected
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid value '{}' for parameter '{}'", ex.getValue(), ex.getName());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", "Invalid value for parameter: " + ex.getName());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Database unique constraint fired unexpectedly
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Database constraint violation", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 409);
        body.put("error", "Conflict");
        body.put("code", "DUPLICATE_VALUE");
        body.put("message", "Email or phone already exists");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Catch-all for anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex) {
        // Spring's own web errors (unknown URL 404, wrong method 405) keep their real status
        if (ex instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
        }

        log.error("Unexpected error", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "Something went wrong");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
        log.warn("Upload too large");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 413);
        body.put("error", "Payload Too Large");
        body.put("message", "File exceeds the 2MB limit");
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }
}