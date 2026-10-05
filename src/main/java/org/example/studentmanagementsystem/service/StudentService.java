package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.StudentRequest;
import org.example.studentmanagementsystem.dto.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);

    List<StudentResponse> createStudents(List<StudentRequest> requests);

    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(int id);

    StudentResponse updateStudent(int id, StudentRequest request);

    void deleteStudent(int id);

    StudentResponse assignDepartment(int studentId, int departmentId);

    StudentResponse enrollCourse(int studentId, int courseId);

    StudentResponse dropCourse(int studentId, int courseId);
}