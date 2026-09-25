package com.example.salarymanagement.service.impl;

import com.example.salarymanagement.entity.*;
import com.example.salarymanagement.repository.*;
import com.example.salarymanagement.service.SeederService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
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
    private final JdbcTemplate jdbcTemplate;

    public SeederServiceImpl(
            DepartmentRepository departmentRepository,
            JobPositionRepository jobPositionRepository,
            CountryRepository countryRepository,
            EmployeeRepository employeeRepository,
            JdbcTemplate jdbcTemplate) {
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.countryRepository = countryRepository;
        this.employeeRepository = employeeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public long seedData() {
        long startTime = System.currentTimeMillis();

        // 1. Seed reference data (idempotent)
        List<Department> departments = seedDepartments();
        List<Country> countries = seedCountries();
        Map<Long, List<JobPosition>> deptPositionsMap = seedJobPositions(departments);

        // 2. Determine how many more employees to seed
        long existingCount = employeeRepository.count();
        int targetCount = 10_000;
        int toSeed = targetCount - (int) existingCount;

        if (toSeed <= 0) {
            log.info("Already have {} employees. Skipping seeding.", existingCount);
            return existingCount;
        }

        log.info("Seeding {} employees using native JDBC batch inserts...", toSeed);

        // 3. Build flat arrays for random access (dept_id, pos_id pairs)
        List<Long> deptIds = new ArrayList<>();
        List<Long> posIds = new ArrayList<>();
        for (Department d : departments) {
            List<JobPosition> posList = deptPositionsMap.get(d.getId());
            if (posList == null) continue;
            for (JobPosition p : posList) {
                deptIds.add(d.getId());
                posIds.add(p.getId());
            }
        }
        List<String> countryCodes = new ArrayList<>();
        for (Country c : countries) {
            countryCodes.add(c.getCountryCode());
        }

        String[] firstNames = {
            "James", "Mary", "John", "Patricia", "Robert", "Jennifer",
            "Michael", "Linda", "William", "Elizabeth", "David", "Barbara",
            "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah",
            "Charles", "Karen", "Arjun", "Priya", "Wei", "Ana", "Lucas",
            "Emma", "Olivia", "Noah", "Sofia", "Liam"
        };
        String[] lastNames = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
            "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez",
            "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore",
            "Jackson", "Martin", "Mehta", "Sharma", "Zhang", "Silva", "Mueller"
        };

        Random random = new Random(42);
        int batchSize = 500;
        int startIndex = (int) existingCount + 1;

        // 4. Use native JDBC batch insert — bypasses Hibernate IDENTITY generator limitation
        //    which silently disables JPA batch inserts for GenerationType.IDENTITY
        List<Object[]> empRows = new ArrayList<>(batchSize);
        List<Object[]> compRows = new ArrayList<>(batchSize);

        // We'll need actual auto-generated employee IDs for compensation FK.
        // Strategy: insert employees in batch, then query back the last N IDs,
        // then insert compensation in batch referencing those IDs.
        int processed = 0;

        while (processed < toSeed) {
            int currentBatch = Math.min(batchSize, toSeed - processed);
            List<Employee> batchEmployees = new ArrayList<>(currentBatch);

            for (int i = 0; i < currentBatch; i++) {
                int empNum = startIndex + processed + i;
                String fName = firstNames[random.nextInt(firstNames.length)];
                String lName = lastNames[random.nextInt(lastNames.length)];
                int pairIdx = random.nextInt(deptIds.size());
                Long deptId = deptIds.get(pairIdx);
                Long posId = posIds.get(pairIdx);
                String countryCode = countryCodes.get(random.nextInt(countryCodes.size()));
                LocalDate joinDate = LocalDate.now().minusDays(random.nextInt(3650));

                Employee emp = new Employee();
                emp.setEmpCode(String.format("EMP-%05d", empNum));
                emp.setFirstName(fName);
                emp.setLastName(lName);
                emp.setEmail(String.format("%s.%s.%d@acme.com", fName.toLowerCase(), lName.toLowerCase(), empNum));
                emp.setPhoneNumber(String.format("+1-555-%04d", random.nextInt(10000)));
                emp.setDepartment(entityManager.getReference(Department.class, deptId));
                emp.setJobPosition(entityManager.getReference(JobPosition.class, posId));
                emp.setCountry(entityManager.getReference(Country.class, countryCode));
                emp.setStatus("ACTIVE");
                emp.setDateOfJoining(joinDate);

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
                batchEmployees.add(emp);
            }

            try {
                employeeRepository.saveAll(batchEmployees);
                entityManager.flush();
                entityManager.clear();
                processed += currentBatch;
                log.info("Seeded {}/{} employees...", processed, toSeed);
            } catch (Exception e) {
                log.error("Failed seeding at index {}, error: {}", processed, e.getMessage(), e);
                throw new RuntimeException("Seeding failed at " + processed, e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully seeded {} employees in {} ms", toSeed, duration);
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
