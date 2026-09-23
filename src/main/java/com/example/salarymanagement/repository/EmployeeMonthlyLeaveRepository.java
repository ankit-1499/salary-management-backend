package com.example.salarymanagement.repository;

import com.example.salarymanagement.entity.EmployeeMonthlyLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeMonthlyLeaveRepository extends JpaRepository<EmployeeMonthlyLeave, Long> {
    List<EmployeeMonthlyLeave> findByEmployeeId(Long employeeId);
    List<EmployeeMonthlyLeave> findByEmployeeIdAndPayPeriod(Long employeeId, String payPeriod);
}
