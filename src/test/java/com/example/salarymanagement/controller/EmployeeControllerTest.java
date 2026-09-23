package com.example.salarymanagement.controller;

import com.example.salarymanagement.config.SecurityConfig;
import com.example.salarymanagement.dto.CompensationDTO;
import com.example.salarymanagement.dto.EmployeeRequestDTO;
import com.example.salarymanagement.dto.EmployeeResponseDTO;
import com.example.salarymanagement.exception.DuplicateResourceException;
import com.example.salarymanagement.exception.GlobalExceptionHandler;
import com.example.salarymanagement.exception.ResourceNotFoundException;
import com.example.salarymanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmployeeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeResponseDTO sampleResponse;
    private EmployeeRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        CompensationDTO compDTO = new CompensationDTO(
                new BigDecimal("100000.00"),
                new BigDecimal("8000.00"),
                new BigDecimal("2000.00"),
                20, 10, new BigDecimal("110000.00")
        );

        sampleResponse = new EmployeeResponseDTO(
                1L, "EMP-001", "John", "Doe", "john.doe@example.com",
                "+1-555-0100", 1L, "Engineering", "ENG",
                1L, "Software Engineer", "USA", "United States", "USD",
                "ACTIVE", LocalDate.now(), compDTO
        );

        validRequest = new EmployeeRequestDTO(
                "EMP-001", "John", "Doe", "john.doe@example.com",
                "+1-555-0100", 1L, 1L, "USA", "ACTIVE",
                LocalDate.now(), compDTO
        );
    }

    @Test
    @DisplayName("401 Unauthorized: When missing security authentication")
    void getEmployees_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("200 OK: Fetching paginated employees with HR role")
    void getEmployees_Success() throws Exception {
        when(employeeService.getEmployees(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].empCode").value("EMP-001"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("201 Created: Successful employee creation")
    void createEmployee_Success() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.empCode").value("EMP-001"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("400 Bad Request: Validation failure or Duplicate Resource Exception")
    void createEmployee_BadRequest() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Employee email already exists"));

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Employee email already exists"));
    }

    @Test
    @WithMockUser(roles = "HR")
    @DisplayName("404 Not Found: Requesting non-existent employee ID")
    void getEmployeeById_NotFound() throws Exception {
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        mockMvc.perform(get("/api/v1/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found with id: 999"));
    }
}
