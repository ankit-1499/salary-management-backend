package com.example.salarymanagement.config;

import com.example.salarymanagement.entity.*;
import com.example.salarymanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Seeds the database with reference data (departments, countries, job positions) on first startup.
 * Employee data is seeded on-demand via the /api/v1/seed endpoint.
 * Idempotent: skips seeding if data already exists.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @Transactional
    CommandLineRunner seedDatabase(
            DepartmentRepository departmentRepo,
            CountryRepository countryRepo,
            JobPositionRepository jobPositionRepo
    ) {
        return args -> {
            // ── 1. Departments ──────────────────────────────────────────
            if (departmentRepo.count() == 0) {
                List<Department> departments = List.of(
                    new Department("Engineering", "ENG"),
                    new Department("Human Resources", "HR"),
                    new Department("Finance", "FIN"),
                    new Department("Marketing", "MKT"),
                    new Department("Sales", "SLS")
                );
                List<Department> savedDepts = departmentRepo.saveAll(departments);
                log.info("Seeded {} departments.", savedDepts.size());

                // ── 2. Job Positions (only after departments are created) ─
                Map<String, Department> deptMap = new HashMap<>();
                for (Department d : savedDepts) {
                    deptMap.put(d.getCode(), d);
                }

                List<JobPosition> positions = new ArrayList<>();
                positions.add(new JobPosition("Senior Software Engineer", deptMap.get("ENG")));
                positions.add(new JobPosition("QA Lead Engineer", deptMap.get("ENG")));
                positions.add(new JobPosition("DevOps Architect", deptMap.get("ENG")));
                positions.add(new JobPosition("HR Generalist", deptMap.get("HR")));
                positions.add(new JobPosition("Talent Acquisition Manager", deptMap.get("HR")));
                positions.add(new JobPosition("Senior Financial Analyst", deptMap.get("FIN")));
                positions.add(new JobPosition("Chief Accountant", deptMap.get("FIN")));
                positions.add(new JobPosition("Growth Marketing Manager", deptMap.get("MKT")));
                positions.add(new JobPosition("Content Strategist", deptMap.get("MKT")));
                positions.add(new JobPosition("Enterprise Sales Executive", deptMap.get("SLS")));
                jobPositionRepo.saveAll(positions);
                log.info("Seeded {} job positions.", positions.size());
            } else {
                log.info("Departments already exist — skipping reference data seeding.");
            }

            // ── 3. Countries ────────────────────────────────────────────
            if (countryRepo.count() == 0) {
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
                countryRepo.saveAll(countries);
                log.info("Seeded {} countries.", countries.size());
            }

            log.info("Reference data ready. Use POST /api/v1/seed to seed 10,000 employee records.");
        };
    }
}
