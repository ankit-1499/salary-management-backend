package com.example.salarymanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "compensation")
public class Compensation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "base_pay", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePay;

    @Column(name = "pf_deduction", nullable = false, precision = 12, scale = 2)
    private BigDecimal pfDeduction;

    @Column(name = "other_deductions", nullable = false, precision = 12, scale = 2)
    private BigDecimal otherDeductions;

    @Column(name = "paid_leaves_allowance", nullable = false)
    private Integer paidLeavesAllowance;

    @Column(name = "sick_leaves_allowance", nullable = false)
    private Integer sickLeavesAllowance;

    @Column(name = "total_company_cost", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal totalCompanyCost;

    public Compensation() {}

    public Compensation(Long id, Employee employee, BigDecimal basePay, BigDecimal pfDeduction, BigDecimal otherDeductions, Integer paidLeavesAllowance, Integer sickLeavesAllowance, BigDecimal totalCompanyCost) {
        this.id = id;
        this.employee = employee;
        this.basePay = basePay;
        this.pfDeduction = pfDeduction;
        this.otherDeductions = otherDeductions;
        this.paidLeavesAllowance = paidLeavesAllowance;
        this.sickLeavesAllowance = sickLeavesAllowance;
        this.totalCompanyCost = totalCompanyCost;
    }

    public Compensation(Employee employee, BigDecimal basePay, BigDecimal pfDeduction, BigDecimal otherDeductions, Integer paidLeavesAllowance, Integer sickLeavesAllowance) {
        this.employee = employee;
        this.basePay = basePay;
        this.pfDeduction = pfDeduction;
        this.otherDeductions = otherDeductions;
        this.paidLeavesAllowance = paidLeavesAllowance;
        this.sickLeavesAllowance = sickLeavesAllowance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getBasePay() {
        return basePay;
    }

    public void setBasePay(BigDecimal basePay) {
        this.basePay = basePay;
    }

    public BigDecimal getPfDeduction() {
        return pfDeduction;
    }

    public void setPfDeduction(BigDecimal pfDeduction) {
        this.pfDeduction = pfDeduction;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public Integer getPaidLeavesAllowance() {
        return paidLeavesAllowance;
    }

    public void setPaidLeavesAllowance(Integer paidLeavesAllowance) {
        this.paidLeavesAllowance = paidLeavesAllowance;
    }

    public Integer getSickLeavesAllowance() {
        return sickLeavesAllowance;
    }

    public void setSickLeavesAllowance(Integer sickLeavesAllowance) {
        this.sickLeavesAllowance = sickLeavesAllowance;
    }

    public BigDecimal getTotalCompanyCost() {
        return totalCompanyCost;
    }

    public void setTotalCompanyCost(BigDecimal totalCompanyCost) {
        this.totalCompanyCost = totalCompanyCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compensation that = (Compensation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
