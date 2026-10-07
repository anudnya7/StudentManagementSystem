package org.example.studentmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.config.CacheNames;
import org.example.studentmanagementsystem.dto.EnrollmentApplyRequest;
import org.example.studentmanagementsystem.dto.EnrollmentResponse;
import org.example.studentmanagementsystem.entity.Course;
import org.example.studentmanagementsystem.entity.EnrollmentRequest;
import org.example.studentmanagementsystem.entity.EnrollmentStatus;
import org.example.studentmanagementsystem.entity.Notification;
import org.example.studentmanagementsystem.entity.Student;
import org.example.studentmanagementsystem.exception.BusinessException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.repository.CourseRepository;
import org.example.studentmanagementsystem.repository.EnrollmentRequestRepository;
import org.example.studentmanagementsystem.repository.NotificationRepository;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.example.studentmanagementsystem.service.EnrollmentRequestService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Teaching centre-piece for "many repository saves in ONE transaction".
 * approve() writes to four tables (courses, students/student_courses, enrollment_requests,
 * notifications) inside a single @Transactional method.
 * If any step throws a RuntimeException, Spring rolls back every save made earlier in that call.
 *
 * Class level readOnly = true is the safe default for the read methods.
 * Methods that write override it with a plain @Transactional.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentRequestServiceImpl implements EnrollmentRequestService {

    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final NotificationRepository notificationRepository;

    // ---------- student applies ----------

    @Override
    @Transactional
    public EnrollmentResponse apply(EnrollmentApplyRequest request) {
        log.info("Student {} applying for course {}", request.studentId(), request.courseId());

        Student student = findStudent(request.studentId());
        Course course = findCourse(request.courseId());

        if (student.getDepartment() == null) {
            throw new BusinessException("Assign a department to the student before applying for courses",
                    "NO_DEPARTMENT", HttpStatus.CONFLICT);
        }
        if (!course.getDepartment().getId().equals(student.getDepartment().getId())) {
            throw new BusinessException("Course " + course.getCode() + " is not in the student's department",
                    "COURSE_NOT_IN_DEPARTMENT", HttpStatus.CONFLICT);
        }
        if (student.getCourses().contains(course)) {
            throw new BusinessException("Student is already enrolled in " + course.getCode(),
                    "ALREADY_ENROLLED", HttpStatus.CONFLICT);
        }
        if (enrollmentRequestRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), course.getId(), EnrollmentStatus.PENDING)) {
            throw new BusinessException("A pending request for " + course.getCode() + " already exists",
                    "REQUEST_ALREADY_PENDING", HttpStatus.CONFLICT);
        }

        EnrollmentRequest saved = enrollmentRequestRepository.saveAndFlush(EnrollmentRequest.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.PENDING)
                .build());

        log.info("Enrollment request created with id: {}", saved.getId());
        return toResponse(saved);
    }

    // ---------- reads ----------

    @Override
    public EnrollmentResponse getById(int id) {
        return toResponse(findRequest(id));
    }

    @Override
    public List<EnrollmentResponse> getAll(EnrollmentStatus status) {
        List<EnrollmentRequest> requests = status == null
                ? enrollmentRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                : enrollmentRequestRepository.findByStatusOrderByIdDesc(status);
        return requests.stream().map(this::toResponse).toList();
    }

    @Override
    public List<EnrollmentResponse> getByStudent(int studentId) {
        findStudent(studentId); // 404 if the student does not exist
        return enrollmentRequestRepository.findByStudentIdOrderByIdDesc(studentId)
                .stream().map(this::toResponse).toList();
    }

    // ---------- department approves / rejects ----------

    /**
     * FOUR saves, ONE transaction:
     * 1. course.enrolledCount + 1
     * 2. student gets the course
     * 3. request becomes APPROVED
     * 4. notification row
     * Either all four are committed, or none of them.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STUDENTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.COURSES, allEntries = true)
    })
    public EnrollmentResponse approve(int id) {
        // lock the request: a double click cannot approve it twice
        EnrollmentRequest request = findRequestForUpdate(id);
        requirePending(request);

        // lock the course: two approvals cannot take the last seat together
        Course course = courseRepository.findByIdForUpdate(request.getCourse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Student student = request.getStudent();

        if (student.getDepartment() == null
                || !course.getDepartment().getId().equals(student.getDepartment().getId())) {
            throw new BusinessException("Student is no longer in the department of " + course.getCode(),
                    "COURSE_NOT_IN_DEPARTMENT", HttpStatus.CONFLICT);
        }
        if (student.getCourses().contains(course)) {
            throw new BusinessException("Student is already enrolled in " + course.getCode(),
                    "ALREADY_ENROLLED", HttpStatus.CONFLICT);
        }
        if (course.getEnrolledCount() >= course.getCapacity()) {
            log.warn("Course {} is full ({}/{})", course.getCode(), course.getEnrolledCount(), course.getCapacity());
            throw new BusinessException("Course is full: " + course.getTitle(),
                    "COURSE_FULL", HttpStatus.CONFLICT);
        }

        // --- save 1: course ---
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);

        // --- save 2: student ---
        student.getCourses().add(course);
        studentRepository.save(student);

        // --- save 3: the request itself ---
        request.setStatus(EnrollmentStatus.APPROVED);
        EnrollmentRequest updated = enrollmentRequestRepository.saveAndFlush(request);

        // --- save 4: notification ---
        notificationRepository.save(Notification.builder()
                .recipientEmail(student.getEmail())
                .message("Your enrollment in " + course.getTitle() + " has been approved.")
                .build());

        log.info("Enrollment request {} approved: student {} -> course {} ({}/{})", id,
                student.getId(), course.getId(), course.getEnrolledCount(), course.getCapacity());
        return toResponse(updated);
    }

    /**
     * Only TWO saves: nothing was reserved while the request was PENDING,
     * so there is nothing to give back.
     */
    @Override
    @Transactional
    public EnrollmentResponse reject(int id) {
        EnrollmentRequest request = findRequestForUpdate(id);
        requirePending(request);

        request.setStatus(EnrollmentStatus.REJECTED);
        EnrollmentRequest updated = enrollmentRequestRepository.saveAndFlush(request);

        notificationRepository.save(Notification.builder()
                .recipientEmail(request.getStudent().getEmail())
                .message("Your enrollment in " + request.getCourse().getTitle() + " was rejected.")
                .build());

        log.info("Enrollment request {} rejected", id);
        return toResponse(updated);
    }

    // ---------- helpers ----------

    private void requirePending(EnrollmentRequest request) {
        if (request.getStatus() != EnrollmentStatus.PENDING) {
            throw new BusinessException(
                    "Enrollment request " + request.getId() + " is already " + request.getStatus(),
                    "REQUEST_ALREADY_PROCESSED", HttpStatus.CONFLICT);
        }
    }

    private EnrollmentRequest findRequest(int id) {
        return enrollmentRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment request not found with id: " + id));
    }

    private EnrollmentRequest findRequestForUpdate(int id) {
        return enrollmentRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment request not found with id: " + id));
    }

    private Student findStudent(int id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private Course findCourse(int id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private EnrollmentResponse toResponse(EnrollmentRequest r) {
        return new EnrollmentResponse(
                r.getId(),
                r.getStudent().getId(),
                r.getStudent().getFirstName() + " " + r.getStudent().getLastName(),
                r.getCourse().getId(),
                r.getCourse().getTitle(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getCreatedBy(),
                r.getUpdatedAt(),
                r.getUpdatedBy());
    }
}