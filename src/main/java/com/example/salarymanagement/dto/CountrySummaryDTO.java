package com.example.salarymanagement.dto;

import java.math.BigDecimal;

public record CountrySummaryDTO(
    String countryCode,
    String countryName,
    Long headcount,
    BigDecimal averageBaseSalary,
    BigDecimal totalCtcSpend
) {}
