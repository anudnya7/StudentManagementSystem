package org.example.studentmanagementsystem.service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public DepartmentResponse create(DepartmentRequest r) {
        if (departmentRepository.existsByNameIgnoreCase(r.name()))
            throw new BusinessException("Department already exists: " + r.name(), "DEPARTMENT_EXISTS", HttpStatus.CONFLICT);
        if (departmentRepository.existsByCodeIgnoreCase(r.code()))
            throw new BusinessException("Department code already exists: " + r.code(), "DEPARTMENT_CODE_EXISTS", HttpStatus.CONFLICT);
        Department saved = departmentRepository.save(
                Department.builder().name(r.name()).code(r.code().toUpperCase()).build());
        log.info("Department created with id: {}", saved.getId());
        return toResponse(saved);
    }

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DepartmentResponse getById(int id) {
        return toResponse(find(id));
    }

    @CacheEvict(value = "students", allEntries = true)   // student responses contain the department name
    public DepartmentResponse update(int id, DepartmentRequest r) {
        Department d = find(id);
        if (!d.getName().equalsIgnoreCase(r.name()) && departmentRepository.existsByNameIgnoreCase(r.name()))
            throw new BusinessException("Department already exists: " + r.name(), "DEPARTMENT_EXISTS", HttpStatus.CONFLICT);
        if (!d.getCode().equalsIgnoreCase(r.code()) && departmentRepository.existsByCodeIgnoreCase(r.code()))
            throw new BusinessException("Department code already exists: " + r.code(), "DEPARTMENT_CODE_EXISTS", HttpStatus.CONFLICT);
        d.setName(r.name());
        d.setCode(r.code().toUpperCase());
        return toResponse(departmentRepository.save(d));
    }

    public void delete(int id) {
        Department d = find(id);
        if (courseRepository.existsByDepartmentId(id) || studentRepository.existsByDepartmentId(id))
            throw new BusinessException("Department has courses or students and cannot be deleted",
                    "DEPARTMENT_IN_USE", HttpStatus.CONFLICT);
        departmentRepository.delete(d);
        log.info("Department deleted with id: {}", id);
    }

    private Department find(int id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(d.getId(), d.getName(), d.getCode());
    }
}