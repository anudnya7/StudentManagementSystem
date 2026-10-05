package org.example.studentmanagementsystem.repository;

import jakarta.persistence.criteria.Predicate;
import org.example.studentmanagementsystem.entity.Student;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class StudentSpecifications {

    private StudentSpecifications() {
    }

    public static Specification<Student> filter(String keyword, Integer departmentId, Integer courseId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String text = keyword.trim();
                String like = "%" + text.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(root.get("phone"), "%" + text + "%")));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (courseId != null) {
                query.distinct(true);
                predicates.add(cb.equal(root.join("courses").get("id"), courseId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}