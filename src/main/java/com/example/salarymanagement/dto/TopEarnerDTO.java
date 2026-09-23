package com.example.salarymanagement.dto;

import java.math.BigDecimal;

public record TopEarnerDTO(
    Long id,
    String empCode,
    String firstName,
    String lastName,
    String departmentName,
    String countryCode,
    String countryName,
    BigDecimal basePay,
    BigDecimal totalCompanyCost
) {}
