public interface CourseRepository extends JpaRepository<Course, Integer> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByDepartmentId(Integer departmentId);
    List<Course> findByDepartmentId(Integer departmentId);
}