package com.example.salarymanagement.service.impl;

import com.example.salarymanagement.dto.CompensationDTO;
import com.example.salarymanagement.dto.EmployeeRequestDTO;
import com.example.salarymanagement.dto.EmployeeResponseDTO;
import com.example.salarymanagement.dto.SalaryUpdateDTO;
import com.example.salarymanagement.entity.*;
import com.example.salarymanagement.exception.DuplicateResourceException;
import com.example.salarymanagement.exception.ResourceNotFoundException;
import com.example.salarymanagement.repository.*;
import com.example.salarymanagement.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final CountryRepository countryRepository;
    private final CompensationRepository compensationRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            JobPositionRepository jobPositionRepository,
            CountryRepository countryRepository,
            CompensationRepository compensationRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.countryRepository = countryRepository;
        this.compensationRepository = compensationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getEmployees(Long departmentId, String countryCode, String status, String search, Pageable pageable) {
        String searchPattern = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String statusVal = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        String countryVal = (countryCode != null && !countryCode.trim().isEmpty()) ? countryCode.trim() : null;

        return employeeRepository.findAllFiltered(departmentId, countryVal, statusVal, searchPattern, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findWithCompensationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToResponseDTO(employee);
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        if (employeeRepository.existsByEmail(requestDTO.email())) {
            throw new DuplicateResourceException("Employee with email '" + requestDTO.email() + "' already exists");
        }
        if (employeeRepository.existsByEmpCode(requestDTO.empCode())) {
            throw new DuplicateResourceException("Employee with code '" + requestDTO.empCode() + "' already exists");
        }

        Department department = departmentRepository.findById(requestDTO.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + requestDTO.departmentId()));

        JobPosition jobPosition = jobPositionRepository.findById(requestDTO.jobPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Job position not found with id: " + requestDTO.jobPositionId()));

        Country country = countryRepository.findById(requestDTO.countryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with code: " + requestDTO.countryCode()));

        Employee employee = new Employee();
        employee.setEmpCode(requestDTO.empCode());
        employee.setFirstName(requestDTO.firstName());
        employee.setLastName(requestDTO.lastName());
        employee.setEmail(requestDTO.email());
        employee.setPhoneNumber(requestDTO.phoneNumber());
        employee.setDepartment(department);
        employee.setJobPosition(jobPosition);
        employee.setCountry(country);
        employee.setStatus(requestDTO.status());
        employee.setDateOfJoining(requestDTO.dateOfJoining());

        CompensationDTO compDTO = requestDTO.compensation();
        Compensation compensation = new Compensation();
        compensation.setBasePay(compDTO.basePay());
        compensation.setPfDeduction(compDTO.pfDeduction());
        compensation.setOtherDeductions(compDTO.otherDeductions());
        compensation.setPaidLeavesAllowance(compDTO.paidLeavesAllowance());
        compensation.setSickLeavesAllowance(compDTO.sickLeavesAllowance());

        employee.setCompensation(compensation);
        Employee saved = employeeRepository.save(employee);
        return mapToResponseDTO(saved);
    }

    @Override
    public EmployeeResponseDTO updateEmployeeSalary(Long id, SalaryUpdateDTO updateDTO) {
        Employee employee = employeeRepository.findWithCompensationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        Compensation compensation = employee.getCompensation();
        if (compensation == null) {
            compensation = new Compensation();
            compensation.setEmployee(employee);
            employee.setCompensation(compensation);
        }

        compensation.setBasePay(updateDTO.basePay());
        compensation.setPfDeduction(updateDTO.pfDeduction());
        compensation.setOtherDeductions(updateDTO.otherDeductions());
        compensation.setPaidLeavesAllowance(updateDTO.paidLeavesAllowance());
        compensation.setSickLeavesAllowance(updateDTO.sickLeavesAllowance());

        if (updateDTO.status() != null && !updateDTO.status().trim().isEmpty()) {
            employee.setStatus(updateDTO.status().trim());
        }

        employeeRepository.save(employee);
        return mapToResponseDTO(employee);
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        Compensation comp = employee.getCompensation();
        CompensationDTO compDTO = null;

        if (comp != null) {
            BigDecimal totalCtc = comp.getTotalCompanyCost();
            if (totalCtc == null && comp.getBasePay() != null) {
                totalCtc = comp.getBasePay()
                        .add(comp.getPfDeduction() != null ? comp.getPfDeduction() : BigDecimal.ZERO)
                        .add(comp.getOtherDeductions() != null ? comp.getOtherDeductions() : BigDecimal.ZERO);
            }

            compDTO = new CompensationDTO(
                    comp.getBasePay(),
                    comp.getPfDeduction(),
                    comp.getOtherDeductions(),
                    comp.getPaidLeavesAllowance(),
                    comp.getSickLeavesAllowance(),
                    totalCtc
            );
        }

        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getEmpCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getDepartment() != null ? employee.getDepartment().getCode() : null,
                employee.getJobPosition() != null ? employee.getJobPosition().getId() : null,
                employee.getJobPosition() != null ? employee.getJobPosition().getTitle() : null,
                employee.getCountry() != null ? employee.getCountry().getCountryCode() : null,
                employee.getCountry() != null ? employee.getCountry().getCountryName() : null,
                employee.getCountry() != null ? employee.getCountry().getCurrencyCode() : null,
                employee.getStatus(),
                employee.getDateOfJoining(),
                compDTO
        );
    }
}
