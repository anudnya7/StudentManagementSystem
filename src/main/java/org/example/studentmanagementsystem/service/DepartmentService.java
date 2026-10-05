package org.example.studentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studentmanagementsystem.dto.DepartmentRequest;
import org.example.studentmanagementsystem.dto.DepartmentResponse;
import org.example.studentmanagementsystem.entity.Department;
import org.example.studentmanagementsystem.exception.BusinessException;
import org.example.studentmanagementsystem.exception.ResourceNotFoundException;
import org.example.studentmanagementsystem.repository.CourseRepository;
import org.example.studentmanagementsystem.repository.DepartmentRepository;
import org.example.studentmanagementsystem.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public DepartmentResponse create(DepartmentRequest r) {
        log.info("Creating department: {}", r.code());
        if (departmentRepository.existsByNameIgnoreCase(r.name())) {
            throw new BusinessException("Department already exists: " + r.name(),
                    "DEPARTMENT_EXISTS", HttpStatus.CONFLICT);
        }
        if (departmentRepository.existsByCodeIgnoreCase(r.code())) {
            throw new BusinessException("Department code already exists: " + r.code(),
                    "DEPARTMENT_CODE_EXISTS", HttpStatus.CONFLICT);
        }
        Department saved = departmentRepository.save(Department.builder()
                .name(r.name())
                .code(r.code().toUpperCase())
                .build());
        log.info("Department created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        log.debug("Fetching all departments");
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(int id) {
        log.debug("Fetching department with id: {}", id);
        return toResponse(find(id));
    }

    public DepartmentResponse update(int id, DepartmentRequest r) {
        log.info("Updating department with id: {}", id);
        Department d = find(id);

        if (!d.getName().equalsIgnoreCase(r.name())
                && departmentRepository.existsByNameIgnoreCase(r.name())) {
            throw new BusinessException("Department already exists: " + r.name(),
                    "DEPARTMENT_EXISTS", HttpStatus.CONFLICT);
        }
        if (!d.getCode().equalsIgnoreCase(r.code())
                && departmentRepository.existsByCodeIgnoreCase(r.code())) {
            throw new BusinessException("Department code already exists: " + r.code(),
                    "DEPARTMENT_CODE_EXISTS", HttpStatus.CONFLICT);
        }

        d.setName(r.name());
        d.setCode(r.code().toUpperCase());
        return toResponse(departmentRepository.save(d));
    }

    public void delete(int id) {
        log.info("Deleting department with id: {}", id);
        Department d = find(id);
        if (courseRepository.existsByDepartmentId(id) || studentRepository.existsByDepartmentId(id)) {
            throw new BusinessException("Department has courses or students and cannot be deleted",
                    "DEPARTMENT_IN_USE", HttpStatus.CONFLICT);
        }
        departmentRepository.delete(d);
        log.info("Department deleted with id: {}", id);
    }

    private Department find(int id) {
        log.trace("Looking up department, id: {}", id);
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(d.getId(), d.getName(), d.getCode());
    }
}