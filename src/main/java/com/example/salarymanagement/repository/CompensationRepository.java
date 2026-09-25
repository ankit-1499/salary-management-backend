package com.example.salarymanagement.repository;

import com.example.salarymanagement.dto.CountrySummaryDTO;
import com.example.salarymanagement.dto.DepartmentSummaryDTO;
import com.example.salarymanagement.dto.TopEarnerDTO;
import com.example.salarymanagement.entity.Compensation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompensationRepository extends JpaRepository<Compensation, Long> {

    Optional<Compensation> findByEmployeeId(Long employeeId);

    @Query("SELECT SUM(c.basePay + c.pfDeduction + c.otherDeductions) FROM Compensation c WHERE UPPER(c.employee.status) = 'ACTIVE'")
    BigDecimal getTotalCompanyCost();

    @Query("SELECT AVG(c.basePay) FROM Compensation c WHERE UPPER(c.employee.status) = 'ACTIVE'")
    BigDecimal getGlobalAverageBaseSalary();

    @Query("SELECT COUNT(e) FROM Employee e WHERE UPPER(e.status) = 'ACTIVE'")
    Long getActiveHeadcount();

    @Query("SELECT (c.basePay + c.pfDeduction + c.otherDeductions) FROM Compensation c WHERE UPPER(c.employee.status) = 'ACTIVE' ORDER BY (c.basePay + c.pfDeduction + c.otherDeductions) ASC")
    List<BigDecimal> getAllSortedCtcValues();

    @Query("""
        SELECT new com.example.salarymanagement.dto.CountrySummaryDTO(
            e.country.countryCode,
            e.country.countryName,
            COUNT(e.id),
            AVG(c.basePay),
            SUM(c.basePay + c.pfDeduction + c.otherDeductions)
        )
        FROM Employee e JOIN e.compensation c
        WHERE UPPER(e.status) = 'ACTIVE'
        GROUP BY e.country.countryCode, e.country.countryName
        ORDER BY SUM(c.basePay + c.pfDeduction + c.otherDeductions) DESC
    """)
    List<CountrySummaryDTO> getCountryBreakdown();

    @Query("""
        SELECT new com.example.salarymanagement.dto.DepartmentSummaryDTO(
            e.department.id,
            e.department.name,
            e.department.code,
            COUNT(e.id),
            SUM(c.basePay + c.pfDeduction + c.otherDeductions)
        )
        FROM Employee e JOIN e.compensation c
        WHERE UPPER(e.status) = 'ACTIVE'
        GROUP BY e.department.id, e.department.name, e.department.code
        ORDER BY SUM(c.basePay + c.pfDeduction + c.otherDeductions) DESC
    """)
    List<DepartmentSummaryDTO> getDepartmentBreakdown();

    @Query("""
        SELECT new com.example.salarymanagement.dto.TopEarnerDTO(
            e.id,
            e.empCode,
            e.firstName,
            e.lastName,
            e.department.name,
            e.country.countryCode,
            e.country.countryName,
            c.basePay,
            (c.basePay + c.pfDeduction + c.otherDeductions)
        )
        FROM Employee e JOIN e.compensation c
        WHERE UPPER(e.status) = 'ACTIVE'
        ORDER BY (c.basePay + c.pfDeduction + c.otherDeductions) DESC
    """)
    List<TopEarnerDTO> findTopEarnersGlobally(Pageable pageable);

    @Query("""
        SELECT new com.example.salarymanagement.dto.TopEarnerDTO(
            e.id,
            e.empCode,
            e.firstName,
            e.lastName,
            e.department.name,
            e.country.countryCode,
            e.country.countryName,
            c.basePay,
            (c.basePay + c.pfDeduction + c.otherDeductions)
        )
        FROM Employee e JOIN e.compensation c
        WHERE e.country.countryCode = :countryCode AND UPPER(e.status) = 'ACTIVE'
        ORDER BY (c.basePay + c.pfDeduction + c.otherDeductions) DESC
    """)
    List<TopEarnerDTO> findTopEarnersByCountry(@Param("countryCode") String countryCode, Pageable pageable);
}
