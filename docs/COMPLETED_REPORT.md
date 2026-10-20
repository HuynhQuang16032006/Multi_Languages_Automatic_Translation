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

---

# Cập nhật lần 2 (P0-1, P0-3, P1-2, P1-5, P2-1..P2-4)

Trạng thái mới (đã kiểm chứng bằng `.\mvnw.cmd clean test`):

| Mã | Nội dung | Trạng thái | Bằng chứng |
|----|----------|------------|-----------|
| P0-1 | Spring Security + JWT | Done | `JwtServiceTest` (5), `AuthIntegrationTest` (5): 401 khi thiếu/sai token, 200 khi login đúng |
| P0-3 | Flyway + `ddl-auto: validate` | Done | `V1__initial_schema.sql`; đã chạy Flyway + validate trên **database rỗng** `audiotour_migration_probe` (Flyway apply v1, Hibernate validate PASS) |
| P1-2 | `docker-compose.yml` + `.env.example` | Viết xong | **KHÔNG verify** — máy không có Docker |
| P1-5 | `docs/ROLLBACK.md` | Done | Tài liệu rollback app + restore DB + repair `flyway_schema_history` |
| P2-1 | CRUD Language | Done | `LanguageServiceTest` (10), `LanguageControllerTest` (9) |
| P2-2 | Phân trang trên `/api/v1` | Done | `PaginationIntegrationTest` (6); `/api/...` vẫn trả mảng phẳng |
| P2-3 | API versioning | Done | `/api/v1/locations`, `/api/v1/tours`, `/api/v1/languages`, `/api/v1/auth/login` |
| P2-4 | Seed data | Không cần thêm dữ liệu | `SeedDataIntegrityTest` (4) pass; mọi narration đã seed đều có file MP3 thật |

## Thay đổi chính

### `pom.xml`
- Thêm `spring-boot-starter-security`, `jjwt-api/impl/jackson` (0.12.6).
- Thêm `flyway-core`, `flyway-mysql` và `spring-boot-flyway`.
- Lý do: Spring Boot 4 tách auto-configuration Flyway sang module `spring-boot-flyway`; chỉ thêm `flyway-core` thì Flyway **không tự chạy** (đã gặp lỗi `Schema validation: missing table [languages]` khi test trên DB rỗng).

### `src/main/resources/application.yml`
- `ddl-auto: update` → `validate`.
- Thêm `spring.flyway` (`baseline-on-migrate: true`, `baseline-version: 0`).
- Thêm `jwt.secret` / `jwt.expiration-ms` / `app.admin.*` với giá trị dev mặc định.

### `src/main/resources/db/migration/V1__initial_schema.sql`
- Tạo 6 bảng: `languages`, `locations`, `tours`, `tour_locations`, `narrations`, `playback_logs`.
- Dùng `CREATE TABLE IF NOT EXISTS` nên là no-op trên DB đã có sẵn.
- Cột/kiểu cột sinh từ chính Hibernate (script DDL tạm) để khớp `validate`.

### Mã nguồn mới
- `security/JwtService`, `security/JwtAuthenticationFilter`, `config/SecurityConfig`, `service/AuthService`, `controller/AuthController`, `dto/LoginRequest`, `dto/LoginResponse`, `exception/UnauthorizedException`.
- `service/LanguageService`, `controller/LanguageController`, `dto/LanguageRequest`.

### Sửa lỗi phát hiện khi viết test
- `GET /api/tours` trước đây **lỗi 500/400**: `LazyInitializationException` trên `Location.subLocations` (open-in-view=false). Đã sửa bằng `@JsonIgnoreProperties({"subLocations"})` trên `Tour.locations` (giữ đúng pattern sẵn có của `Location.subLocations`). Không fetch lồng 2 bag để tránh `MultipleBagFetchException`.

### Docs / CI
- `README.md`: bảng API thêm login/language/`/api/v1`, ghi rõ yêu cầu `Authorization: Bearer`, sửa section Docker/Env bị lỗi `\n`, cập nhật số test.
- `.github/workflows/ci.yml`: thêm `JWT_SECRET`, `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`.
- `docs/ROLLBACK.md`, `docker-compose.yml`, `.env.example` (mới).

## Chưa verify
- `docker-compose.yml` và Dockerfile: không chạy được (máy không có Docker).
- Kiểm chứng Flyway trên DB rỗng dùng database tạm `audiotour_migration_probe` (đã `flyway clean`, DB rỗng còn lại trên MySQL local).

---

# Cập nhật lần 3 (review cuối)

| Mã | Nội dung | Bằng chứng |
|----|----------|------------|
| R1 | Bật profile `prod` thật sự | compose set `SPRING_PROFILES_ACTIVE: prod`; `.env.example` để trống secret; README mục "Chạy production"; `ProdProfileTest` boot profile `prod` + login bằng biến môi trường |
| R2 | Bỏ fail-open | `SecurityConfig`: `anyRequest().denyAll()` (không cần permit `/error`) |
| R3 | Filter không chạy 2 lần | bỏ `@Component`, khởi tạo trong `SecurityConfig`; `AuthIntegrationTest.testJwtFilterRunsOnceAndStaysInSecurityChain` |
| R4 | Chặn username rỗng/ngắn | `AuthService` guard `APP_ADMIN_USERNAME`; `AuthServiceTest.testBlankOrTooShortAdminUsernameIsRejected` |
| R5 | Sửa mô tả OpenAPI | `LanguageController` ghi 409 thay vì 400 |
| R6 | Số test & mã lỗi README | 75/75 và "409 Conflict nếu trùng `code`" |
| R7 | Test handler thật | `GlobalExceptionHandlerTest.testDataIntegrityViolationMapsTo409WithoutRawSql` |

## Đã defer (biết, chưa sửa)
- **Not-found không nhất quán:** `LanguageService` trả 404; `LocationService` / `TourService` vẫn trả 400 (giữ nguyên có chủ đích).
- **Phân trang fetch-join chạy trong bộ nhớ** (`HHH90003004`): kết quả đúng nhưng tải hết dữ liệu; hướng nâng cấp ghi ở comment `shortcut:` trong `LocationRepository` / `TourRepository`.
- **Chưa có giới hạn số lần đăng nhập sai / lockout / audit log** cho `AuthService`.
- **docker-compose và profile `prod` chưa verify trên container thật** (máy không có Docker); chỉ verify YAML và boot profile `prod` bằng test.
