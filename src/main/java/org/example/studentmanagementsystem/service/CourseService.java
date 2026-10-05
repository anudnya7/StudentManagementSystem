@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    public CourseResponse create(CourseRequest r) {
        if (courseRepository.existsByCodeIgnoreCase(r.code()))
            throw new BusinessException("Course code already exists: " + r.code(), "COURSE_CODE_EXISTS", HttpStatus.CONFLICT);
        Course saved = courseRepository.save(Course.builder()
                .title(r.title()).code(r.code().toUpperCase()).credits(r.credits())
                .department(findDepartment(r.departmentId())).build());
        log.info("Course created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAll(Integer departmentId) {
        List<Course> courses = departmentId == null
                ? courseRepository.findAll()
                : courseRepository.findByDepartmentId(departmentId);
        return courses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(int id) {
        return toResponse(find(id));
    }

    @CacheEvict(value = "students", allEntries = true)
    @Transactional
    public CourseResponse update(int id, CourseRequest r) {
        Course c = find(id);
        if (!c.getCode().equalsIgnoreCase(r.code()) && courseRepository.existsByCodeIgnoreCase(r.code()))
            throw new BusinessException("Course code already exists: " + r.code(), "COURSE_CODE_EXISTS", HttpStatus.CONFLICT);
        if (!c.getDepartment().getId().equals(r.departmentId()) && studentRepository.existsByCoursesId(id))
            throw new BusinessException("Students are enrolled, so the department cannot change",
                    "COURSE_HAS_STUDENTS", HttpStatus.CONFLICT);
        c.setTitle(r.title());
        c.setCode(r.code().toUpperCase());
        c.setCredits(r.credits());
        c.setDepartment(findDepartment(r.departmentId()));
        return toResponse(courseRepository.save(c));
    }

    @CacheEvict(value = "students", allEntries = true)
    public void delete(int id) {
        Course c = find(id);
        if (studentRepository.existsByCoursesId(id))
            throw new BusinessException("Students are enrolled in this course", "COURSE_HAS_STUDENTS", HttpStatus.CONFLICT);
        courseRepository.delete(c);
    }

    private Course find(int id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private Department findDepartment(int id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getTitle(), c.getCode(), c.getCredits(),
                c.getDepartment().getId(), c.getDepartment().getName());
    }
}