package com.example.salarymanagement.dto;

import java.math.BigDecimal;

public record SalaryAnalyticsDTO(
    BigDecimal totalCompanyCost,
    BigDecimal globalAverageSalary,
    BigDecimal medianSalary,
    Long activeHeadcount
) {}
