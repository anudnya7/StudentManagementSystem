package org.example.studentmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studentmanagementsystem.config.OpenApiConfig;
import org.example.studentmanagementsystem.dto.EnrollmentApplyRequest;
import org.example.studentmanagementsystem.dto.EnrollmentResponse;
import org.example.studentmanagementsystem.entity.EnrollmentStatus;
import org.example.studentmanagementsystem.service.EnrollmentRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Enrollment Requests", description = "A student applies for a course, the department approves or rejects")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/enrollment-requests")
@RequiredArgsConstructor
public class EnrollmentRequestController {

    private final EnrollmentRequestService enrollmentRequestService;

    @Operation(summary = "Apply for a course", description = "Creates a PENDING request. Nothing is reserved yet.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Request created"),
            @ApiResponse(responseCode = "404", description = "Student or course not found"),
            @ApiResponse(responseCode = "409", description = "No department, wrong department, already enrolled or already pending")
    })
    @PostMapping
    public ResponseEntity<EnrollmentResponse> apply(@Valid @RequestBody EnrollmentApplyRequest request) {
        return new ResponseEntity<>(enrollmentRequestService.apply(request), HttpStatus.CREATED);
    }

    @Operation(summary = "List requests", description = "Optionally filter by status: PENDING, APPROVED or REJECTED.")
    @GetMapping
    public List<EnrollmentResponse> getAll(
            @Parameter(description = "PENDING, APPROVED or REJECTED") @RequestParam(required = false) EnrollmentStatus status) {
        return enrollmentRequestService.getAll(status);
    }

    @Operation(summary = "Get one request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public EnrollmentResponse getById(@PathVariable int id) {
        return enrollmentRequestService.getById(id);
    }

    @Operation(summary = "All requests of one student")
    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> getByStudent(@PathVariable int studentId) {
        return enrollmentRequestService.getByStudent(studentId);
    }

    @Operation(summary = "Approve a request",
            description = "One transaction, four saves: course seat count, student enrolment, request status, notification.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Approved"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Course full or request already processed")
    })
    @PostMapping("/{id}/approve")
    public EnrollmentResponse approve(@PathVariable int id) {
        return enrollmentRequestService.approve(id);
    }

    @Operation(summary = "Reject a request", description = "One transaction, two saves: request status and notification.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rejected"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Request already processed")
    })
    @PostMapping("/{id}/reject")
    public EnrollmentResponse reject(@PathVariable int id) {
        return enrollmentRequestService.reject(id);
    }
}