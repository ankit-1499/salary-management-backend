package com.example.salarymanagement.service;

import com.example.salarymanagement.dto.CompensationDTO;
import com.example.salarymanagement.dto.EmployeeRequestDTO;
import com.example.salarymanagement.dto.EmployeeResponseDTO;
import com.example.salarymanagement.dto.SalaryUpdateDTO;
import com.example.salarymanagement.entity.Country;
import com.example.salarymanagement.entity.Department;
import com.example.salarymanagement.entity.Employee;
import com.example.salarymanagement.entity.JobPosition;
import com.example.salarymanagement.exception.DuplicateResourceException;
import com.example.salarymanagement.exception.ResourceNotFoundException;
import com.example.salarymanagement.repository.*;
import com.example.salarymanagement.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private JobPositionRepository jobPositionRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private CompensationRepository compensationRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private JobPosition jobPosition;
    private Country country;
    private EmployeeRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "Engineering", "ENG");
        jobPosition = new JobPosition(1L, "Software Engineer", department);
        country = new Country("USA", "United States", "USD");

        CompensationDTO compDTO = new CompensationDTO(
                new BigDecimal("100000.00"),
                new BigDecimal("8000.00"),
                new BigDecimal("2000.00"),
                20, 10, null
        );

        validRequest = new EmployeeRequestDTO(
                "EMP-001", "John", "Doe", "john.doe@example.com",
                "+1-555-0100", 1L, 1L, "USA", "ACTIVE",
                LocalDate.now(), compDTO
        );
    }

    @Test
    @DisplayName("Happy Path: Successful employee creation and correct CTC mapping")
    void createEmployee_Success() {
        when(employeeRepository.existsByEmail(validRequest.email())).thenReturn(false);
        when(employeeRepository.existsByEmpCode(validRequest.empCode())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(jobPositionRepository.findById(1L)).thenReturn(Optional.of(jobPosition));
        when(countryRepository.findById("USA")).thenReturn(Optional.of(country));

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(100L);
            return emp;
        });

        EmployeeResponseDTO response = employeeService.createEmployee(validRequest);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("EMP-001", response.empCode());
        assertEquals("john.doe@example.com", response.email());
        assertNotNull(response.compensation());
        assertEquals(new BigDecimal("100000.00"), response.compensation().basePay());
        assertEquals(new BigDecimal("110000.00"), response.compensation().totalCompanyCost());
    }

    @Test
    @DisplayName("Edge Case: Attempting to create employee with duplicate email or empCode")
    void createEmployee_DuplicateException() {
        when(employeeRepository.existsByEmail(validRequest.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.createEmployee(validRequest));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Worst Case: Fetching or updating a non-existent employee ID")
    void getEmployeeById_NotFoundException() {
        when(employeeRepository.findWithCompensationById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(999L));
        
        SalaryUpdateDTO updateDTO = new SalaryUpdateDTO(new BigDecimal("50000"), new BigDecimal("1000"), new BigDecimal("500"), 10, 5, null);
        assertThrows(ResourceNotFoundException.class, () -> employeeService.updateEmployeeSalary(999L, updateDTO));
    }
}
