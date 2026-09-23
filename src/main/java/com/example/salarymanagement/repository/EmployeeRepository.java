package com.example.salarymanagement.repository;

import com.example.salarymanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @EntityGraph(attributePaths = {"department", "jobPosition", "country", "compensation"})
    Optional<Employee> findWithCompensationById(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmpCode(String empCode);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmpCodeAndIdNot(String empCode, Long id);

    @EntityGraph(attributePaths = {"department", "jobPosition", "country", "compensation"})
    @Query("""
        SELECT e FROM Employee e
        WHERE (:departmentId IS NULL OR e.department.id = :departmentId)
          AND (:countryCode IS NULL OR e.country.countryCode = :countryCode)
          AND (:status IS NULL OR e.status = :status)
          AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(e.empCode) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Employee> findAllFiltered(
        @Param("departmentId") Long departmentId,
        @Param("countryCode") String countryCode,
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable
    );
}
