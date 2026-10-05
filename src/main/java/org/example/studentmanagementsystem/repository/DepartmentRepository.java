public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCase(String code);
}