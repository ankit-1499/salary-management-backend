# Requirements Document - Salary Management System Backend

## System Overview
The backend service for ACME Organization's Salary Management System provides APIs for Employee Management, HR Executive Analytics, and High-Performance 10,000 Record Seeding.

## Technology Stack
- Java 17
- Spring Boot 3.x
- Spring Security (HTTP Basic Auth, `ROLE_HR`)
- Spring Data JPA & Hibernate 6
- MySQL 8.0+ / H2 In-Memory DB
- JUnit 5 & Mockito

## API Specifications

### 1. Employee Management API
- `GET /api/v1/employees`: Paginated list filterable by `departmentId`, `countryCode`, `status`, and `search`.
- `GET /api/v1/employees/{id}`: Detailed employee profile with compensation breakdown.
- `POST /api/v1/employees`: Create new employee profile and initial compensation.
- `PUT /api/v1/employees/{id}/salary`: Update employee salary and leave allowances.

### 2. HR Executive Analytics API
- `GET /api/v1/analytics/summary`: Aggregate total company CTC, global average salary, median salary, active headcount.
- `GET /api/v1/analytics/breakdown/country`: Aggregate headcount, average base salary, total CTC spend per country.
- `GET /api/v1/analytics/breakdown/department`: Aggregate headcount and total CTC spend per department.
- `GET /api/v1/analytics/top-earners`: Top N highest-paid employees globally and per country.

### 3. Data Seeder API
- `POST /api/v1/seed`: Trigger high-performance JDBC batch seeding of 10,000 employees.

### 4. Health Check
- `GET /actuator/health`: Publicly accessible status check endpoint.
