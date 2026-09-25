package com.example.salarymanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SalaryUpdateDTO(
    @NotNull(message = "Base pay is required")
    @Min(value = 0, message = "Base pay cannot be negative")
    BigDecimal basePay,

    @NotNull(message = "PF deduction is required")
    @Min(value = 0, message = "PF deduction cannot be negative")
    BigDecimal pfDeduction,

    @NotNull(message = "Other deductions is required")
    @Min(value = 0, message = "Other deductions cannot be negative")
    BigDecimal otherDeductions,

    @NotNull(message = "Paid leaves allowance is required")
    @Min(value = 0, message = "Paid leaves allowance cannot be negative")
    Integer paidLeavesAllowance,

    @NotNull(message = "Sick leaves allowance is required")
    @Min(value = 0, message = "Sick leaves allowance cannot be negative")
    Integer sickLeavesAllowance,

    String status
) {}
