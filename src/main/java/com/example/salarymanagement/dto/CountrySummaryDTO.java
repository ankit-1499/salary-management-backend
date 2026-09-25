package com.example.salarymanagement.dto;

import java.math.BigDecimal;

public record CountrySummaryDTO(
    String countryCode,
    String countryName,
    Long headcount,
    BigDecimal averageBaseSalary,
    BigDecimal totalCtcSpend
) {
    public CountrySummaryDTO(String countryCode, String countryName, Long headcount, Double averageBaseSalary, BigDecimal totalCtcSpend) {
        this(
            countryCode,
            countryName,
            headcount,
            averageBaseSalary != null ? BigDecimal.valueOf(averageBaseSalary) : BigDecimal.ZERO,
            totalCtcSpend != null ? totalCtcSpend : BigDecimal.ZERO
        );
    }
}

