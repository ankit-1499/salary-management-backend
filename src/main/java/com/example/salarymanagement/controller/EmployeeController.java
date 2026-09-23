package com.example.salarymanagement.controller;

import com.example.salarymanagement.dto.EmployeeRequestDTO;
import com.example.salarymanagement.dto.EmployeeResponseDTO;
import com.example.salarymanagement.dto.SalaryUpdateDTO;
import com.example.salarymanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EmployeeResponseDTO> employees = employeeService.getEmployees(departmentId, countryCode, status, search, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO created = employeeService.createEmployee(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/salary")
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeSalary(
            @PathVariable Long id,
            @Valid @RequestBody SalaryUpdateDTO updateDTO) {
        EmployeeResponseDTO updated = employeeService.updateEmployeeSalary(id, updateDTO);
        return ResponseEntity.ok(updated);
    }
}
