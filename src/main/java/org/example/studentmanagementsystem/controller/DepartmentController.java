package org.example.studentmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.studentmanagementsystem.config.OpenApiConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.dto.DepartmentRequest;
import org.example.studentmanagementsystem.dto.DepartmentResponse;
import org.example.studentmanagementsystem.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Departments", description = "Departments own courses and students")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final Validator validator;

    @Operation(summary = "Create a department", description = "Code and name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Duplicate")
    })
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return new ResponseEntity<>(departmentService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "List departments", description = "Returns every department. Cached in Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned")
    })
    @GetMapping
    public List<DepartmentResponse> getAll() {
        return departmentService.getAll();
    }

    @Operation(summary = "Get one department", description = "Cached in Redis by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable int id) {
        return departmentService.getById(id);
    }

    @Operation(summary = "Replace a department", description = "All fields are required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate")
    })
    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable int id,
                                     @Valid @RequestBody DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    @Operation(summary = "Partially update a department", description = "Send only the fields to change.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable int id,
                                   @RequestBody DepartmentRequest patch) {

        // 1. Load current values (404 if the id doesn't exist)
        DepartmentResponse current = departmentService.getById(id);

        // 2. Merge: sent value wins, otherwise keep current
        DepartmentRequest merged = new DepartmentRequest(
                patch.name() != null ? patch.name() : current.name(),
                patch.code() != null ? patch.code() : current.code()
        );

        // 3. Validate the merged result with the same rules as POST/PUT
        Set<ConstraintViolation<DepartmentRequest>> violations = validator.validate(merged);
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

        // 4. Reuse the existing update (also checks duplicate name and code)
        return ResponseEntity.ok(departmentService.update(id, merged));
    }

    @Operation(summary = "Delete a department", description = "Blocked while it is still in use.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Still in use")
    })
    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        departmentService.delete(id);
        return "Department deleted successfully";
    }
}