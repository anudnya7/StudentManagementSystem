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
import org.example.studentmanagementsystem.dto.CourseRequest;
import org.example.studentmanagementsystem.dto.CourseResponse;
import org.example.studentmanagementsystem.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Courses", description = "Courses, each belonging to one department")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final Validator validator;

    @Operation(summary = "Create a course", description = "Code and name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Duplicate")
    })
    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return new ResponseEntity<>(courseService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "List courses", description = "Optionally filter by departmentId. Cached in Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned")
    })
    @GetMapping
    public List<CourseResponse> getAll(@Parameter(description = "Only courses in this department") @RequestParam(required = false) Integer departmentId) {
        return courseService.getAll(departmentId);
    }

    @Operation(summary = "Get one course", description = "Cached in Redis by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public CourseResponse getById(@PathVariable int id) {
        return courseService.getById(id);
    }

    @Operation(summary = "Replace a course", description = "All fields are required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate")
    })
    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable int id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @Operation(summary = "Partially update a course", description = "Send only the fields to change.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable int id,
                                   @RequestBody CourseRequest patch) {

        // 1. Load current values (404 if the id doesn't exist)
        CourseResponse current = courseService.getById(id);

        // 2. Merge: sent value wins, otherwise keep current
        CourseRequest merged = new CourseRequest(
                patch.title()        != null ? patch.title()        : current.title(),
                patch.code()         != null ? patch.code()         : current.code(),
                patch.credits()      != null ? patch.credits()      : current.credits(),
                patch.departmentId() != null ? patch.departmentId() : current.departmentId()
        );

        // 3. Validate the merged result with the same rules as POST/PUT
        Set<ConstraintViolation<CourseRequest>> violations = validator.validate(merged);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            violations.forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", 400);
            body.put("error", "Validation Failed");
            body.put("errors", errors);
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // 4. Reuse the existing update (duplicate code, enrolled-students rule)
        return ResponseEntity.ok(courseService.update(id, merged));
    }

    @Operation(summary = "Delete a course", description = "Blocked while it is still in use.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Still in use")
    })
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        courseService.delete(id);
        return "Course deleted successfully";
    }
}