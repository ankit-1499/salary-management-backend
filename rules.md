# ACME Organization - Salary Management System Rules

## Architecture Rules
- Use clean multi-layer architecture under `com.example.salarymanagement`:
  - `config`: Security & application configuration.
  - `controller`: REST controllers returning standard `ResponseEntity<T>` envelopes. Controllers MUST NOT contain business logic.
  - `dto`: Immutable Java `record` types or clean DTO classes. Never expose JPA Entities directly via REST endpoints.
  - `entity`: JPA entities mapping strictly to database tables.
  - `exception`: `@RestControllerAdvice` global exception handler returning RFC-compliant JSON responses.
  - `repository`: Spring Data JPA repositories. All aggregations across large datasets MUST be executed in the database layer using native SQL/JPQL queries.
  - `service`: Business logic interfaces and implementations (`EmployeeService`, `AnalyticsService`, `SeederService`).

## Data & ORM Rules
- Database dialect: MySQL 8.0+ for production, H2 in MySQL mode for testing.
- Database name: `acme_salary_db`.
- JPA Entities MUST map strictly to schema:
  - `departments` (`id`, `name`, `code`)
  - `job_positions` (`id`, `title`, `department_id`)
  - `countries` (`country_code`, `country_name`, `currency_code`)
  - `employees` (`id`, `emp_code`, `first_name`, `last_name`, `email`, `phone_number`, `department_id`, `job_position_id`, `country_code`, `status`, `date_of_joining`)
  - `compensation` (`id`, `employee_id`, `base_pay`, `pf_deduction`, `other_deductions`, `paid_leaves_allowance`, `sick_leaves_allowance`, `total_company_cost`)
    - Mark `total_company_cost` as `@Column(insertable = false, updatable = false)`.
  - `employee_monthly_leaves` (`id`, `employee_id`, `pay_period`, `paid_leaves_taken`, `sick_leaves_taken`, `unpaid_leaves_taken`)
- Use `@EntityGraph` or `JOIN FETCH` to eliminate $N+1$ query overhead.

## Performance & Seeding Rules
- Seeding 10,000 realistic records MUST complete within 3-5 seconds using JDBC batching (`hibernate.jdbc.batch_size=500`).
- Analytics endpoints must execute in sub-50ms using DB-level native aggregation queries.

## Testing Rules
- Every service & controller test suite MUST cover at least 3 distinct scenarios:
  1. Happy Path
  2. Edge Case (e.g. duplicates, even/odd medians)
  3. Worst Case (e.g. non-existent ID resource not found, empty dataset handling without division by zero)
