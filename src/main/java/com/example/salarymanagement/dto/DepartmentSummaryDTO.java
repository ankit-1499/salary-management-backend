package com.example.salarymanagement.dto;

import java.math.BigDecimal;

public record DepartmentSummaryDTO(
    Long departmentId,
    String departmentName,
    String departmentCode,
    Long headcount,
    BigDecimal totalCtcSpend
) {}
