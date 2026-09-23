package com.example.salarymanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EmployeeRequestDTO(
    @NotBlank(message = "Employee code is required")
    String empCode,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    String phoneNumber,

    @NotNull(message = "Department ID is required")
    Long departmentId,

    @NotNull(message = "Job position ID is required")
    Long jobPositionId,

    @NotBlank(message = "Country code is required")
    @Size(min = 3, max = 3, message = "Country code must be 3 characters")
    String countryCode,

    @NotBlank(message = "Status is required")
    String status,

    @NotNull(message = "Date of joining is required")
    LocalDate dateOfJoining,

    @NotNull(message = "Compensation details are required")
    @Valid
    CompensationDTO compensation
) {}
