package org.example.studentmanagementsystem.config;

/**
 * One place for cache names. Using constants instead of repeating "students"
 * in many annotations means a typo becomes a compile error, not a silent bug.
 */
public final class CacheNames {

    public static final String STUDENTS = "students";            // one student, key = id
    public static final String STUDENT_PAGES = "studentPages";   // result of /getall, key = all the query params
    public static final String COURSES = "courses";              // course lists and single courses
    public static final String DEPARTMENTS = "departments";      // department lists and single departments

    private CacheNames() {
    }
}