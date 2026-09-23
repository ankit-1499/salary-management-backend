package com.example.salarymanagement.dto;

import java.time.LocalDate;

public record EmployeeResponseDTO(
    Long id,
    String empCode,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    Long departmentId,
    String departmentName,
    String departmentCode,
    Long jobPositionId,
    String jobTitle,
    String countryCode,
    String countryName,
    String currencyCode,
    String status,
    LocalDate dateOfJoining,
    CompensationDTO compensation
) {}
