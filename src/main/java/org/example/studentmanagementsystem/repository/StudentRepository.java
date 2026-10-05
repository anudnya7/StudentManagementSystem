public interface StudentRepository extends JpaRepository<Student, Integer>,
        JpaSpecificationExecutor<Student> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    Optional<Student> findByEmailIgnoreCase(String email);
    boolean existsByDepartmentId(Integer departmentId);
    boolean existsByCoursesId(Integer courseId);
}