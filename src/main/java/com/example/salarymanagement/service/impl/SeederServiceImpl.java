package com.example.salarymanagement.service.impl;

import com.example.salarymanagement.entity.*;
import com.example.salarymanagement.repository.*;
import com.example.salarymanagement.service.SeederService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class SeederServiceImpl implements SeederService {

    private static final Logger log = LoggerFactory.getLogger(SeederServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final CountryRepository countryRepository;
    private final EmployeeRepository employeeRepository;

    public SeederServiceImpl(
            DepartmentRepository departmentRepository,
            JobPositionRepository jobPositionRepository,
            CountryRepository countryRepository,
            EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.countryRepository = countryRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public long seedData() {
        long startTime = System.currentTimeMillis();

        // 1. Seed Departments if empty
        List<Department> departments = seedDepartments();
        
        // 2. Seed Countries if empty
        List<Country> countries = seedCountries();

        // 3. Seed Job Positions if empty
        Map<Long, List<JobPosition>> deptPositionsMap = seedJobPositions(departments);

        // 4. Batch Seed 10,000 Employees with Compensation
        long existingCount = employeeRepository.count();
        int targetCount = 10000;
        int toSeed = targetCount - (int) existingCount;

        if (toSeed <= 0) {
            log.info("Already have {} employees. Skipping seeding.", existingCount);
            return existingCount;
        }

        String[] firstNames = {"James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda", "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};

        Random random = new Random(42);
        int batchSize = 500;
        int startIndex = (int) existingCount + 1;

        for (int i = 0; i < toSeed; i++) {
            int empNum = startIndex + i;
            String fName = firstNames[random.nextInt(firstNames.length)];
            String lName = lastNames[random.nextInt(lastNames.length)];
            
            Department dept = departments.get(random.nextInt(departments.size()));
            List<JobPosition> posList = deptPositionsMap.get(dept.getId());
            JobPosition pos = posList.get(random.nextInt(posList.size()));
            Country ctry = countries.get(random.nextInt(countries.size()));

            Employee emp = new Employee();
            emp.setEmpCode(String.format("EMP-%05d", empNum));
            emp.setFirstName(fName);
            emp.setLastName(lName);
            emp.setEmail(String.format("%s.%s.%d@acme.com", fName.toLowerCase(), lName.toLowerCase(), empNum));
            emp.setPhoneNumber(String.format("+1-555-%04d", random.nextInt(10000)));
            emp.setDepartment(dept);
            emp.setJobPosition(pos);
            emp.setCountry(ctry);
            emp.setStatus("ACTIVE");
            emp.setDateOfJoining(LocalDate.now().minusDays(random.nextInt(3650)));

            double baseVal = 50000 + (random.nextDouble() * 100000);
            BigDecimal basePay = BigDecimal.valueOf(baseVal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pfDeduction = basePay.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal otherDeductions = basePay.multiply(BigDecimal.valueOf(0.04)).setScale(2, RoundingMode.HALF_UP);

            Compensation comp = new Compensation();
            comp.setBasePay(basePay);
            comp.setPfDeduction(pfDeduction);
            comp.setOtherDeductions(otherDeductions);
            comp.setPaidLeavesAllowance(15 + random.nextInt(10));
            comp.setSickLeavesAllowance(10 + random.nextInt(5));

            emp.setCompensation(comp);

            entityManager.persist(emp);

            if ((i + 1) % batchSize == 0 || i == toSeed - 1) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Batch seeded {} employees in {} ms", toSeed, duration);
        return employeeRepository.count();
    }

    private List<Department> seedDepartments() {
        List<Department> existing = departmentRepository.findAll();
        if (!existing.isEmpty()) return existing;

        List<Department> depts = List.of(
            new Department("Engineering", "ENG"),
            new Department("Human Resources", "HR"),
            new Department("Finance", "FIN"),
            new Department("Marketing", "MKT"),
            new Department("Sales", "SLS")
        );
        return departmentRepository.saveAll(depts);
    }

    private List<Country> seedCountries() {
        List<Country> existing = countryRepository.findAll();
        if (!existing.isEmpty()) return existing;

        List<Country> countries = List.of(
            new Country("USA", "United States", "USD"),
            new Country("CAN", "Canada", "CAD"),
            new Country("GBR", "United Kingdom", "GBP"),
            new Country("DEU", "Germany", "EUR"),
            new Country("FRA", "France", "EUR"),
            new Country("IND", "India", "INR"),
            new Country("AUS", "Australia", "AUD"),
            new Country("JPN", "Japan", "JPY"),
            new Country("SGP", "Singapore", "SGD"),
            new Country("BRA", "Brazil", "BRL")
        );
        return countryRepository.saveAll(countries);
    }

    private Map<Long, List<JobPosition>> seedJobPositions(List<Department> departments) {
        List<JobPosition> existing = jobPositionRepository.findAll();
        Map<Long, List<JobPosition>> resultMap = new HashMap<>();

        if (!existing.isEmpty()) {
            for (JobPosition pos : existing) {
                resultMap.computeIfAbsent(pos.getDepartment().getId(), k -> new ArrayList<>()).add(pos);
            }
            return resultMap;
        }

        List<JobPosition> toSave = new ArrayList<>();
        for (Department dept : departments) {
            switch (dept.getCode()) {
                case "ENG" -> {
                    toSave.add(new JobPosition("Senior Software Engineer", dept));
                    toSave.add(new JobPosition("QA Lead Engineer", dept));
                    toSave.add(new JobPosition("DevOps Architect", dept));
                }
                case "HR" -> {
                    toSave.add(new JobPosition("HR Generalist", dept));
                    toSave.add(new JobPosition("Talent Acquisition Manager", dept));
                }
                case "FIN" -> {
                    toSave.add(new JobPosition("Senior Financial Analyst", dept));
                    toSave.add(new JobPosition("Chief Accountant", dept));
                }
                case "MKT" -> {
                    toSave.add(new JobPosition("Growth Marketing Manager", dept));
                    toSave.add(new JobPosition("Content Strategist", dept));
                }
                case "SLS" -> {
                    toSave.add(new JobPosition("Enterprise Sales Executive", dept));
                }
            }
        }
        List<JobPosition> saved = jobPositionRepository.saveAll(toSave);
        for (JobPosition pos : saved) {
            resultMap.computeIfAbsent(pos.getDepartment().getId(), k -> new ArrayList<>()).add(pos);
        }
        return resultMap;
    }
}
