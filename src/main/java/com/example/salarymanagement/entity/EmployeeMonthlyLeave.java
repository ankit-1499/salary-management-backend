package com.example.salarymanagement.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "employee_monthly_leaves")
public class EmployeeMonthlyLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_period", length = 7, nullable = false)
    private String payPeriod;

    @Column(name = "paid_leaves_taken", nullable = false)
    private Integer paidLeavesTaken;

    @Column(name = "sick_leaves_taken", nullable = false)
    private Integer sickLeavesTaken;

    @Column(name = "unpaid_leaves_taken", nullable = false)
    private Integer unpaidLeavesTaken;

    public EmployeeMonthlyLeave() {}

    public EmployeeMonthlyLeave(Long id, Employee employee, String payPeriod, Integer paidLeavesTaken, Integer sickLeavesTaken, Integer unpaidLeavesTaken) {
        this.id = id;
        this.employee = employee;
        this.payPeriod = payPeriod;
        this.paidLeavesTaken = paidLeavesTaken;
        this.sickLeavesTaken = sickLeavesTaken;
        this.unpaidLeavesTaken = unpaidLeavesTaken;
    }

    public EmployeeMonthlyLeave(Employee employee, String payPeriod, Integer paidLeavesTaken, Integer sickLeavesTaken, Integer unpaidLeavesTaken) {
        this.employee = employee;
        this.payPeriod = payPeriod;
        this.paidLeavesTaken = paidLeavesTaken;
        this.sickLeavesTaken = sickLeavesTaken;
        this.unpaidLeavesTaken = unpaidLeavesTaken;
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

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public Integer getPaidLeavesTaken() {
        return paidLeavesTaken;
    }

    public void setPaidLeavesTaken(Integer paidLeavesTaken) {
        this.paidLeavesTaken = paidLeavesTaken;
    }

    public Integer getSickLeavesTaken() {
        return sickLeavesTaken;
    }

    public void setSickLeavesTaken(Integer sickLeavesTaken) {
        this.sickLeavesTaken = sickLeavesTaken;
    }

    public Integer getUnpaidLeavesTaken() {
        return unpaidLeavesTaken;
    }

    public void setUnpaidLeavesTaken(Integer unpaidLeavesTaken) {
        this.unpaidLeavesTaken = unpaidLeavesTaken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeMonthlyLeave that = (EmployeeMonthlyLeave) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
