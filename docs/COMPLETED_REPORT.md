# Completed Work Report (Detailed)

## P0-P3 Status
- P0-1 Security/JWT: not done (blocked by test tooling).
- P0-2 Env credentials: code done, DB verification pending.
- P0-3 Migration: not done.
- P0-4 Validation: done.
- P0-5 Global exception handler: done.
- P1-1 Dockerfile: done.
- P1-2 docker-compose: not done.
- P1-3 CI push image: done.
- P1-4 Deploy staging: not done.
- P1-5 Rollback docs: not done.
- P2-1 CRUD Language: not done.
- P2-2 Pagination: not done.
- P2-3 API versioning: not done.
- P2-4 Seed data: not done.
- P3-1 JaCoCo: done.
- P3-2 Actuator: done.
- P3-3 Structured logging: done.
- P3-4 18 tests green: blocked.

## Modified files

### `.github/workflows/ci.yml`
- Added `docker-build-push` job.
- Builds/pushes Docker image to GHCR after tests pass.
- Purpose: automate container delivery.

### `pom.xml`
- Added `jacoco-maven-plugin` (0.8.12).
- Generates coverage report during `test`.
- Purpose: enforce/monitor code coverage.

### `README.md`
- Added Docker run and env configuration notes.
- Purpose: deployment/onboarding documentation.

## New files

### `Dockerfile`
- Multi-stage build: Maven build + JRE runtime.
- Purpose: containerize Spring Boot app.

### `CONTRIBUTING.md`
- Contribution workflow.
- Purpose: contributor onboarding.

### `docs/BRANCHING.md`
- Branching strategy and protection rules.
- Purpose: git workflow documentation.

### `src/main/resources/logback-spring.xml`
- Console logging pattern.
- Purpose: structured/standardized logs.

### `docs/COMPLETED_REPORT.md`
- This report.

## Previously completed (P0/P3)
- Validation dependency and annotations.
- Global exception handling standardization.
- Env-based DB credentials.
- Actuator endpoints.

## Blocked (detailed)
- P0-1 Security + JWT:
  - Need Spring Security dependency, JWT filter, login/token endpoint.
  - Blocked because baseline tests fail (Mockito cannot mock classes on JDK 21/ByteBuddy).
  - Requires test tooling fix first.
- P3-4 18 tests green:
  - Cannot verify due to Mockito/ByteBuddy + JDK 21 incompatibility.
  - Need `-XX:+EnableDynamicAgentLoading` or dependency alignment.

## Not done / DB required (detailed)
- P0-2 Bỏ hardcode credentials:
  - Code already uses env vars with defaults; full verification needs DB startup.
- P0-3 Migration + ddl-auto validate:
  - Needs Flyway/Liquibase dependency and SQL migration.
- P1-2 docker-compose:
  - Needs app + MySQL integration verification.
- P1-5 Rollback docs:
  - Needs DB restore procedures.
- P2-1 CRUD Language:
  - Needs repository/service/controller implementation and DB.
- P2-2 Pagination:
  - Needs repository Pageable support and DB.
- P2-4 Seed data:
  - Needs DataInitializer updates and DB.