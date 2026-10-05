package org.example.studentmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.studentmanagementsystem.config.OpenApiConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.dto.PageResponse;
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

@Tag(name = "Students", description = "Student CRUD, search, pagination, department and course enrolment")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final Validator validator;

    @Operation(summary = "Create a student", description = "Creates one student without a login.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Duplicate email or phone")
    })
    @PostMapping("/create")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        log.debug("POST /create received");
        return new ResponseEntity<>(studentService.createStudent(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Create many students", description = "All-or-nothing: if one item is invalid or duplicate, none are saved.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "All created"),
            @ApiResponse(responseCode = "409", description = "Duplicate email or phone in batch")
    })
    @PostMapping("/createall")
    public ResponseEntity<List<StudentResponse>> createStudents(
            @Valid @RequestBody List<@Valid StudentRequest> requests) {
        log.debug("POST /createall received with {} items", requests.size());
        return new ResponseEntity<>(studentService.createStudents(requests), HttpStatus.CREATED);
    }

    @Operation(summary = "List students (paged, sorted, searchable)", description = "keyword searches first name, last name, email and phone. departmentId and courseId filter. Results are cached in Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid sortBy, page or size")
    })
    @GetMapping("/getall")
    public PageResponse<StudentResponse> getAllStudents(
            @Parameter(description = "Page number, starts at 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page, 1 to 100") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "id, firstName, lastName, email or dateOfBirth") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "asc or desc") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Search text (name, email, phone)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Only students in this department") @RequestParam(required = false) Integer departmentId,
            @Parameter(description = "Only students enrolled in this course") @RequestParam(required = false) Integer courseId) {
        log.debug("GET /getall received");
        return studentService.getAllStudents(page, size, sortBy, direction, keyword, departmentId, courseId);
    }

    @Operation(summary = "Get one student", description = "Cached in Redis by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @GetMapping("/get/{id}")
    public StudentResponse getStudentById(@PathVariable int id) {
        log.debug("GET /get/{} received", id);
        return studentService.getStudentById(id);
    }

    @Operation(summary = "Replace a student's details", description = "All five fields are required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate email or phone")
    })
    @PutMapping("/update/{id}")
    public StudentResponse updateStudent(@PathVariable int id,
                                         @Valid @RequestBody StudentRequest request) {
        log.debug("PUT /update/{} received", id);
        return studentService.updateStudent(id, request);
    }

    @Operation(summary = "Partially update a student", description = "Send only the fields to change; the rest keep their values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PatchMapping("/patch/{id}")
    public ResponseEntity<?> patchStudent(@PathVariable int id,
                                          @RequestBody StudentRequest patch) {
        log.debug("PATCH /patch/{} received", id);

        StudentResponse current = studentService.getStudentById(id);

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

    @Operation(summary = "Delete a student", description = "Also removes the student's course links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @DeleteMapping("/delete/{id}")
    public String deleteStudent(@PathVariable int id) {
        log.debug("DELETE /delete/{} received", id);
        studentService.deleteStudent(id);
        return "Student deleted successfully";
    }

    @Operation(summary = "Assign a department", description = "Changing department clears the student's courses (they belong to the old department).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned"),
            @ApiResponse(responseCode = "404", description = "Student or department not found")
    })
    @PostMapping("/{id}/department/{departmentId}")
    public StudentResponse assignDepartment(@PathVariable int id, @PathVariable int departmentId) {
        log.debug("Assign department {} to student {}", departmentId, id);
        return studentService.assignDepartment(id, departmentId);
    }

    @Operation(summary = "Enrol in a course", description = "The student needs a department and the course must belong to it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrolled"),
            @ApiResponse(responseCode = "409", description = "No department, wrong department or already enrolled")
    })
    @PostMapping("/{id}/courses/{courseId}")
    public StudentResponse enroll(@PathVariable int id, @PathVariable int courseId) {
        log.debug("Enroll student {} in course {}", id, courseId);
        return studentService.enrollCourse(id, courseId);
    }

    @Operation(summary = "Drop a course", description = "Removes the enrolment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dropped"),
            @ApiResponse(responseCode = "409", description = "Not enrolled")
    })
    @DeleteMapping("/{id}/courses/{courseId}")
    public StudentResponse drop(@PathVariable int id, @PathVariable int courseId) {
        log.debug("Drop course {} for student {}", courseId, id);
        return studentService.dropCourse(id, courseId);
    }
}