package org.example.studentmanagementsystem.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.dto.StudentRequest;
import org.example.studentmanagementsystem.dto.StudentResponse;
import org.example.studentmanagementsystem.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final Validator validator;

    @PostMapping("/create")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        log.debug("POST /create received");
        return new ResponseEntity<>(studentService.createStudent(request), HttpStatus.CREATED);
    }

    @PostMapping("/createall")
    public ResponseEntity<List<StudentResponse>> createStudents(
            @Valid @RequestBody List<@Valid StudentRequest> requests) {
        log.debug("POST /createall received with {} items", requests.size());
        return new ResponseEntity<>(studentService.createStudents(requests), HttpStatus.CREATED);
    }

    @GetMapping("/getall")
    public List<StudentResponse> getAllStudents() {
        log.debug("GET /getall received");
        return studentService.getAllStudents();
    }

    @GetMapping("/get/{id}")
    public StudentResponse getStudentById(@PathVariable int id) {
        log.debug("GET /get/{} received", id);
        return studentService.getStudentById(id);
    }

    @PutMapping("/update/{id}")
    public StudentResponse updateStudent(@PathVariable int id,
                                         @Valid @RequestBody StudentRequest request) {
        log.debug("PUT /update/{} received", id);
        return studentService.updateStudent(id, request);
    }

    @PatchMapping("/patch/{id}")
    public ResponseEntity<?> patchStudent(@PathVariable int id,
                                          @RequestBody StudentRequest patch) {
        log.debug("PATCH /patch/{} received", id);

        StudentResponse current = studentService.getStudentById(id);
        log.trace("PATCH id={} received body: {}", id, patch);

        StudentRequest merged = new StudentRequest(
                patch.firstName()   != null ? patch.firstName()   : current.getFirstName(),
                patch.lastName()    != null ? patch.lastName()    : current.getLastName(),
                patch.email()       != null ? patch.email()       : current.getEmail(),
                patch.phone()       != null ? patch.phone()       : current.getPhone(),
                patch.dateOfBirth() != null ? patch.dateOfBirth() : current.getDateOfBirth()
        );
        log.debug("PATCH id={} merged result ready", id);

        Set<ConstraintViolation<StudentRequest>> violations = validator.validate(merged);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            violations.forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
            log.warn("PATCH validation failed for id {}: {}", id, errors);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", 400);
            body.put("error", "Validation Failed");
            body.put("errors", errors);
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(studentService.updateStudent(id, merged));
    }

    @DeleteMapping("/delete/{id}")
    public String deleteStudent(@PathVariable int id) {
        log.debug("DELETE /delete/{} received", id);
        studentService.deleteStudent(id);
        return "Student deleted successfully";
    }

    // TEMPORARY: triggers an ERROR log. Delete after testing.
    @GetMapping("/test-error")
    public String testError() {
        log.trace("test-error: about to fail");
        throw new IllegalStateException("Simulated failure for log testing");
    }
}