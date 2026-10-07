package org.example.studentmanagementsystem.service;

import org.example.studentmanagementsystem.dto.DepartmentRequest;
import org.example.studentmanagementsystem.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(DepartmentRequest request);

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(int id);

    DepartmentResponse update(int id, DepartmentRequest request);

    void delete(int id);
}