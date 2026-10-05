package org.example.studentmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.dto.PageResponse;
import org.example.studentmanagementsystem.dto.StudentRequest;
import org.example.studentmanagementsystem.dto.StudentResponse;
import org.example.studentmanagementsystem.entity.Course;
import org.example.studentmanagementsystem.entity.Department;
import org.example.studentmanagementsystem.entity.Student;
import org.example.studentmanagementsystem.exception.BusinessException;
import org.example.studentmanagementsystem.exception.EmailAlreadyExistsException;
import org.example.studentmanagementsystem.exception.PhoneAlreadyExistsException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.repository.CourseRepository;
import org.example.studentmanagementsystem.repository.DepartmentRepository;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.example.studentmanagementsystem.repository.StudentSpecifications;
import org.example.studentmanagementsystem.service.StudentService;
import org.springframework.dao.DataAccessException;
import org.example.studentmanagementsystem.config.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "firstName", "lastName", "email", "dateOfBirth");

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    // ---------- CRUD ----------

    @Override
    @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    public StudentResponse createStudent(StudentRequest request) {
        log.info("Creating student with email: {}", request.email());
        log.trace("Checking duplicate email and phone for: {}", request.email());

        if (studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (studentRepository.existsByPhone(request.phone())) {
            throw new PhoneAlreadyExistsException(request.phone());
        }

        Student student = Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .dateOfBirth(request.dateOfBirth())
                .build();

        Student saved = save(student);
        log.info("Student created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    public List<StudentResponse> createStudents(List<StudentRequest> requests) {
        log.info("Bulk create started for {} students", requests.size());

        Set<String> emailsInBatch = new HashSet<>();
        Set<String> phonesInBatch = new HashSet<>();
        List<Student> students = new ArrayList<>();

        for (StudentRequest r : requests) {
            log.trace("Checking batch item with email: {}", r.email());

            if (!emailsInBatch.add(r.email().toLowerCase())
                    || studentRepository.existsByEmailIgnoreCase(r.email())) {
                throw new EmailAlreadyExistsException(r.email());
            }
            if (!phonesInBatch.add(r.phone())
                    || studentRepository.existsByPhone(r.phone())) {
                throw new PhoneAlreadyExistsException(r.phone());
            }

            students.add(Student.builder()
                    .firstName(r.firstName())
                    .lastName(r.lastName())
                    .email(r.email())
                    .phone(r.phone())
                    .dateOfBirth(r.dateOfBirth())
                    .build());
        }
        log.debug("Batch validated, saving {} students", students.size());

        List<Student> savedEntities;
        try {
            savedEntities = studentRepository.saveAll(students);
        } catch (DataAccessException ex) {
            log.error("Bulk save failed for {} students", students.size(), ex);
            throw ex;
        }

        List<StudentResponse> saved = savedEntities.stream().map(this::toResponse).toList();
        log.info("Bulk create saved {} students", saved.size());
        return saved;
    }

    // ---------- pagination, sorting, search ----------

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.STUDENT_PAGES,
            key = "'p' + #page + '-s' + #size + '-' + #sortBy + '-' + #direction + '-k' + #keyword + '-d' + #departmentId + '-c' + #courseId")
    public PageResponse<StudentResponse> getAllStudents(int page, int size, String sortBy, String direction,
                                                        String keyword, Integer departmentId, Integer courseId) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException("Invalid sortBy: " + sortBy + ". Allowed: " + ALLOWED_SORT_FIELDS,
                    "INVALID_SORT_FIELD", HttpStatus.BAD_REQUEST);
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("page must be >= 0 and size must be between 1 and 100",
                    "INVALID_PAGE", HttpStatus.BAD_REQUEST);
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> result = studentRepository.findAll(
                StudentSpecifications.filter(keyword, departmentId, courseId), pageable);

        log.debug("getAllStudents page={} size={} sortBy={} {} -> {} of {} records",
                page, size, sortBy, direction, result.getNumberOfElements(), result.getTotalElements());

        List<StudentResponse> content = result.getContent().stream().map(this::toResponse).toList();

        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.STUDENTS, key = "#id")
    public StudentResponse getStudentById(int id) {
        log.debug("Fetching student with id: {}", id);
        return toResponse(findStudentOrThrow(id));
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.STUDENTS, key = "#id"),
            evict = @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    )
    public StudentResponse updateStudent(int id, StudentRequest request) {
        log.info("Updating student with id: {}", id);
        Student existing = findStudentOrThrow(id);

        if (!existing.getEmail().equalsIgnoreCase(request.email())
                && studentRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (!existing.getPhone().equals(request.phone())
                && studentRepository.existsByPhone(request.phone())) {
            throw new PhoneAlreadyExistsException(request.phone());
        }

        existing.setFirstName(request.firstName());
        existing.setLastName(request.lastName());
        existing.setEmail(request.email());
        existing.setPhone(request.phone());
        existing.setDateOfBirth(request.dateOfBirth());

        Student saved = save(existing);
        log.info("Student updated with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STUDENTS, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    })
    public void deleteStudent(int id) {
        log.debug("Delete requested for id: {}", id);
        Student existing = findStudentOrThrow(id);
        try {
            studentRepository.delete(existing);
        } catch (DataAccessException ex) {
            log.error("Failed to delete student with id: {}", id, ex);
            throw ex;
        }
        log.info("Student deleted with id: {}", id);
    }

    // ---------- department and course logic ----------

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.STUDENTS, key = "#studentId"),
            evict = @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    )
    public StudentResponse assignDepartment(int studentId, int departmentId) {
        Student s = findStudentOrThrow(studentId);
        Department d = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + departmentId));

        if (s.getDepartment() != null && !s.getDepartment().getId().equals(departmentId)) {
            log.info("Student {} changed department, clearing {} courses",
                    studentId, s.getCourses().size());
            s.getCourses().clear();
        }

        s.setDepartment(d);
        log.info("Student {} assigned to department {}", studentId, d.getCode());
        return toResponse(save(s));
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.STUDENTS, key = "#studentId"),
            evict = @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    )
    public StudentResponse enrollCourse(int studentId, int courseId) {
        Student s = findStudentOrThrow(studentId);
        Course c = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (s.getDepartment() == null) {
            throw new BusinessException("Assign a department before enrolling in courses",
                    "NO_DEPARTMENT", HttpStatus.CONFLICT);
        }
        if (!c.getDepartment().getId().equals(s.getDepartment().getId())) {
            throw new BusinessException("Course " + c.getCode() + " is not in the student's department",
                    "COURSE_NOT_IN_DEPARTMENT", HttpStatus.CONFLICT);
        }
        if (!s.getCourses().add(c)) {
            throw new BusinessException("Student already enrolled in " + c.getCode(),
                    "ALREADY_ENROLLED", HttpStatus.CONFLICT);
        }

        log.info("Student {} enrolled in course {}", studentId, c.getCode());
        return toResponse(save(s));
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = CacheNames.STUDENTS, key = "#studentId"),
            evict = @CacheEvict(cacheNames = CacheNames.STUDENT_PAGES, allEntries = true)
    )
    public StudentResponse dropCourse(int studentId, int courseId) {
        Student s = findStudentOrThrow(studentId);
        boolean removed = s.getCourses().removeIf(c -> c.getId().equals(courseId));
        if (!removed) {
            throw new BusinessException("Student is not enrolled in course " + courseId,
                    "NOT_ENROLLED", HttpStatus.CONFLICT);
        }
        log.info("Student {} dropped course {}", studentId, courseId);
        return toResponse(save(s));
    }

    // ---------- helpers ----------

    private Student save(Student student) {
        try {
            return studentRepository.saveAndFlush(student);
        } catch (DataAccessException ex) {
            log.error("Database error while saving student", ex);
            throw ex;
        }
    }

    private Student findStudentOrThrow(int id) {
        log.trace("Looking up student in database, id: {}", id);
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private StudentResponse toResponse(Student s) {
        int age = Period.between(s.getDateOfBirth(), LocalDate.now()).getYears();
        log.trace("Mapping student id {} to response (age {})", s.getId(), age);

        return StudentResponse.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .fullName(s.getFirstName() + " " + s.getLastName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .dateOfBirth(s.getDateOfBirth())
                .age(age)
                .departmentName(s.getDepartment() != null ? s.getDepartment().getName() : null)
                .courses(s.getCourses().stream().map(Course::getTitle).sorted().toList())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .build();
    }
}