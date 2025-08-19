# Leave Management System

A Spring Boot application (Java 17) for managing employee leave requests, approvals and balances. It demonstrates typical enterprise patterns: layered services, DTOs, JPA repositories, JWT authentication and simple role-based authorization.

Tech stack
- Java 17
- Spring Boot (Web, Data JPA, Security)
- Spring Security with JWT
- Lombok for DTO/entity boilerplate
- MySQL (production) or H2 (local testing)

---

## Quick overview
- Authentication: JWT (stateless).
- Authorization: role-based (EMPLOYEE, HR).
- Primary user flows:
  - Employees register/login, apply for leave, view their balance and leave history.
  - HR users approve/reject leave requests and can view all requests.

---

## Checklist (what this README covers)
- Setup & run (Windows PowerShell)
- Configuration required (DB, JWT)
- Assumptions made by the codebase
- Edge cases the application currently handles
- Potential improvements and next steps

---

## Setup (Windows PowerShell)
1. Ensure Java 17 and Maven are installed. Set `JAVA_HOME`.
2. From project root (where `pom.xml` is located):

```powershell
# compile
.\mvnw.cmd clean compile

# run in-place
.\mvnw.cmd spring-boot:run

# or build jar then run
.\mvnw.cmd package
java -jar target\Leave-management-system-0.0.1-SNAPSHOT.jar
```

3. If you prefer to run tests:

```powershell
.\mvnw.cmd test
```

---

## Configuration
Edit `src/main/resources/application.properties` before running.

Minimum values to set:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/leave_management
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

Recommended additions:

```properties
# externalize JWT secret
spring.jwt.secret=replace-with-a-strong-random-secret
```

Note: `JwtUtil` should read the secret from configuration (`@Value("${spring.jwt.secret}")`) rather than using a hard-coded value.

For quick local development without MySQL, use H2:

```properties
spring.datasource.url=jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## API (high-level)
Base URL: `http://localhost:8080`

- POST `/api/auth/register` — register a user (Employee). Ensure password is hashed before saving.
- POST `/api/auth/login` — returns `{ token, role }` on success.
- POST `/api/leaves` — apply for leave (requires auth, EMPLOYEE/HR roles allowed).
- PUT `/api/leaves/{id}/approve` — approve a leave (HR only).
- PUT `/api/leaves/{id}/reject` — reject a leave (HR only).
- GET `/api/leaves` — list leaves (supports pagination and filtering by status/employee).
- GET `/api/employees/{id}/balance` — get current leave balance for an employee.

Example: apply for leave

```json
POST /api/leaves
{
  "employeeId": 1,
  "startDate": "2025-09-01",
  "endDate": "2025-09-03",
  "reason": "Vacation",
  "type": "ANNUAL"
}
```

Include header: `Authorization: Bearer <jwt>`

---

## Assumptions made by the codebase
- Employees have a unique email used as username.
- Roles are represented as simple strings checked by `SecurityConfig` (currently `EMPLOYEE` and `HR`).
- `Employee` entity contains (or should contain) `password` and `role` fields for authentication/authorization.
- Annual allocation and joining date are used to compute balances and validate apply requests.
- JWT tokens are short-lived and stateless (no server-side session storage).

---

## Edge cases handled (in current code)
- Overlapping requests: new apply is rejected if overlapping PENDING/APPROVED leaves exist for the same employee.
- Insufficient balance: application rejected if requested days exceed remaining allocation for the year.
- Joining date validation: cannot apply for leave before joining date.
- Self-approval prevention: approver cannot approve their own requests.
- Only PENDING leaves may be approved/rejected — other transitions are disallowed.

---

## Potential improvements (recommended)
1. Security
  - Externalize JWT secret to `application.properties` and load via `@Value`.
  - Ensure `Employee` passwords are stored hashed with BCrypt. Update registration to encode passwords.
  - Use standard ROLE_ prefix for authorities or normalize checks consistently across the app.

2. Robustness & UX
  - Add comprehensive unit and integration tests (service layer, controllers, security flows).
  - Provide a seed/fixture process for test users (HR and Employee) and sample data.
  - Add OpenAPI/Swagger documentation for the API.

3. Deployment & DevOps
  - Add Dockerfile and docker-compose for MySQL + app to simplify local and CI runs.
  - Add Flyway/Liquibase for deterministic schema migrations instead of `hibernate.ddl-auto=update`.
  - Add CI pipeline (GitHub Actions) to run tests and build artifacts.

4. Observability
  - Add structured logging and request tracing.
  - Add health and metrics endpoints for monitoring.

5. Security hardening
  - Rate limit authentication endpoints.
  - Rotate JWT secret and implement token revocation/blacklisting if needed.

---

## Troubleshooting & common fixes
- Lombok: enable annotation processing in your IDE.
- Package mismatch: standardize repository package to `com.example.Leave_management_system.repository` (lowercase) — fix files under `src/main/java/.../repository`.
- JwtUtil missing package: add `package com.example.Leave_management_system.security;` at top of `JwtUtil.java` if absent.
- If you see compilation errors about missing getters (e.g., `getType()`), confirm DTO fields and Lombok annotations are present.

---

## Contact / Contributing
- If you plan to submit this repo, ensure the configuration values are sanitized (no secrets committed). Add a CONTRIBUTING.md if multiple people will collaborate.

If you want, I can also:
- Apply the JwtUtil package fix and re-run a build here,
- Add a simple `docker-compose.yml` to run MySQL + app for local testing,
- Add a minimal seed `CommandLineRunner` that creates an HR user.

---

This README is intended to be concise and submission-ready. Tell me if you want any section expanded (API examples, Postman collection, or diagrams).
