package com.example.salarymanagement.service;

import com.example.salarymanagement.dto.EmployeeRequestDTO;
import com.example.salarymanagement.dto.EmployeeResponseDTO;
import com.example.salarymanagement.dto.SalaryUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    Page<EmployeeResponseDTO> getEmployees(Long departmentId, String countryCode, String status, String search, Pageable pageable);
    EmployeeResponseDTO getEmployeeById(Long id);
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO);
    EmployeeResponseDTO updateEmployeeSalary(Long id, SalaryUpdateDTO updateDTO);
}
