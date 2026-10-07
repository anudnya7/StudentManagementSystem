package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.PageResponse;
import org.example.studentmanagementsystem.dto.StudentRequest;
import org.example.studentmanagementsystem.dto.StudentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);

    List<StudentResponse> createStudents(List<StudentRequest> requests);

    PageResponse<StudentResponse> getAllStudents(int page, int size, String sortBy, String direction,
                                                 String keyword, Integer departmentId, Integer courseId);

    StudentResponse getStudentById(int id);

    StudentResponse updateStudent(int id, StudentRequest request);

    void deleteStudent(int id);

    StudentResponse assignDepartment(int studentId, int departmentId);

    StudentResponse enrollCourse(int studentId, int courseId);

    StudentResponse dropCourse(int studentId, int courseId);

    StudentResponse uploadImage(int studentId, MultipartFile file);
}