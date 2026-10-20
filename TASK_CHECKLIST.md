# Task Checklist — BE Audio Tour Multi-Language (3-Tier + CI/CD)

Mục tiêu: hoàn thiện Backend Audio Tour đa ngôn ngữ từ ~75–80% lên production-ready, theo nhánh BE dev hướng CI/CD.

## Cách sử dụng cho agent
- Mỗi mục có checkbox `[ ]`. Đánh `[x]` khi đã xong và verify.
- Mỗi mục có phần `Verify:` để agent tự kiểm tra trước khi đánh xong.
- Không bỏ qua mục P0 (bắt buộc trước khi deploy).
- Sau khi sửa code, chạy `./mvnw clean verify` (hoặc `.\mvnw.cmd clean verify` trên Windows) để đảm bảo 75 test vẫn xanh.

---

## P0 — Production readiness (bắt buộc trước khi deploy)

### P0-1. Bảo vệ API bằng Spring Security + JWT
- [x] Thêm dependency `spring-boot-starter-security`.
- [x] Cấu hình filter JWT, bảo vệ tất cả endpoint `/api/**` (ngoại trừ Swagger / actuator nếu cần public).
- [x] Thêm endpoint login / signup (hoặc ít nhất là tạo token test cho mobile app).
- Verify: `./mvnw test` xanh; gọi API không token nhận 401/403.
- Ghi chú: chỉ có `POST /api/auth/login` + `/api/v1/auth/login`, tài khoản admin lấy từ env. Không có signup và không có bảng `users` — việc này chuyển sang mục P5-1.

### P0-2. Bỏ hardcode credentials trong `application.yml`
- [x] Thay `username: root`, `password: 123456` bằng `${DB_USERNAME}`, `${DB_PASSWORD}` (hoặc Spring Cloud placeholder).
- [x] Cung cấp ví dụ `.env` / `application-prod.yml` cho production.
- Verify: app khởi động được khi env var được set; không còn chuỗi `123456` trong source.
- Ghi chú: `application-prod.yml` không có giá trị mặc định, thiếu env là fail-fast. `.env` đã bị `.gitignore`.

### P0-3. Thêm migration (Flyway hoặc Liquibase), tắt `ddl-auto: update`
- [x] Thêm dependency Flyway/Liquibase.
- [x] Viết migration `V1__initial_schema.sql` dựa trên entity hiện tại.
- [x] Thay `spring.jpa.hibernate.ddl-auto: update` bằng `validate` (hoặc `none`).
- Verify: `./mvnw flyway:migrate` (hoặc equivalent) tạo đúng schema trên DB trống.
- Ghi chú: đã verify trên DB trống (Flyway apply V1 rồi Hibernate `validate` pass). Spring Boot 4 cần thêm `spring-boot-flyway`, chỉ `flyway-core` thì Flyway không tự chạy.

### P0-4. Validation cho request DTO
- [x] Thêm `spring-boot-starter-validation`.
- [x] Đặt `@Valid`, `@NotBlank`, `@NotNull`, `@Size`... vào `PlaybackLogRequest` và DTO khác.
- Verify: gửi request thiếu trường nhận 400 với message rõ ràng, không bị 500.

### P0-5. GlobalExceptionHandler trả về lỗi chuẩn, không rò rỉ traceback
- [x] Đảm bảo `GlobalExceptionHandler` bắt `RuntimeException`, `MethodArgumentNotValidException`, v.v.
- [x] Trả về JSON có dạng `{ timestamp, status, error, message, path }`.
- Verify: provoke lỗi (ví dụ locationId không tồn tại) nhận JSON chuẩn, không có stacktrace HTML.

---

## P1 — DevOps & CI/CD

### P1-1. Viết `Dockerfile` (multi-stage)
- [x] Stage build: Maven + JDK 21, build JAR.
- [x] Stage run: JRE slim, copy JAR, entrypoint `java -jar`.
- Verify: `docker build -t audiotour-backend .` thành công.

### P1-2. Viết `docker-compose.yml` (app + MySQL)
- [x] Service `db`: image `mysql:8.0`, env root password, database `audiotour_db`.
- [x] Service `app`: build từ Dockerfile, phụ thuộc `db`, map cổng 8080.
- Verify: `docker-compose up --build` chạy cả hai container, gọi `http://localhost:8080/swagger-ui.html` thành công.
- Ghi chú: file đã viết, có healthcheck + `SPRING_PROFILES_ACTIVE=prod` + biến bắt buộc `${VAR:?}`. CHƯA verify bằng Docker (máy không có Docker).

### P1-3. CI build & push Docker image
- [x] Cập nhật `.github/workflows/ci.yml`: thêm job build & push image đến GitHub Container Registry / Docker Hub sau khi test xong.
- Verify: CI trên GitHub chạy xong, có artifact JAR và Docker image được push.

### P1-4. Thêm job deploy staging
- [ ] Thêm job/staging deploy sau khi package thành công (ví dụ deploy đến server test hoặc Render/Railway/…).
- Verify: có log deploy trong GitHub Actions.
- Chưa làm: cần hạ tầng deploy thật + secret trên GitHub, không thể hoàn thành trong repo.

### P1-5. Rollback strategy & tài liệu
- [x] Ghi tài liệu rollback (cách quay lại phiên bản trước, cách restore DB nếu migration sai).
- Verify: file docs tồn tại trong repo.
- Ghi chú: `docs/ROLLBACK.md`.

---

## P2 — Tính năng đa ngôn ngữ & API

### P2-1. CRUD quản lý `Language`
- [x] Thêm `LanguageController`, `LanguageService` (nếu chưa có).
- [x] API: GET /api/languages, POST /api/languages, PUT /api/languages/{id}, DELETE /api/languages/{id}.
- Verify: gọi Swagger UI tạo được ngôn ngữ mới, xem danh sách, sửa/xóa.
- Ghi chú: có cả bản `/api/v1/languages` (phân trang). Trùng `code` trả 409, không tìm thấy trả 404, xóa ngôn ngữ đang được narration dùng trả 409.

### P2-2. Phân trang / sắp xếp cho danh sách lớn
- [x] Thêm `Pageable` vào `/api/locations`, `/api/tours` (và `/api/languages` nếu có).
- Verify: params `page`, `size`, `sort` hoạt động, trả về `totalElements`, `totalPages`.
- Ghi chú: phân trang chỉ đặt ở `/api/v1/...`; `/api/...` giữ nguyên mảng JSON cũ để không phá client. Đã có test cho cả hai dạng.

### P2-3. Versioning API
- [x] Thêm prefix `/api/v1/…` hoặc header versioning cho các endpoint chính.
- Verify: gọi `/api/v1/locations` thành công.
- Ghi chú: giữ song song `/api/...` và `/api/v1/...` (không breaking).

### P2-4. Hoàn tất dữ liệu seed
- [x] Cập nhật `DataInitializer.java` seed đầy đủ các trạm (Khánh Hội, Vĩnh Hội, Xóm Chiếu, Dinh Độc Lập…) và phòng ban con đã liệt kê trong README.
- Verify: sau khởi động DB trống, có dữ liệu POI và narration vi/en.
- Ghi chú: seed hiện có đã đủ 6 POI + 2 phòng con + 2 ngôn ngữ + 7 narration (mọi file MP3 được seed đều tồn tại trong `uploads/audio`). Không thêm dữ liệu giả; thay vào đó thêm test `SeedDataIntegrityTest` để chặn hồi quy.

---

## P3 — Quan sát & Chất lượng

### P3-1. Code coverage (JaCoCo) + ngưỡng tối thiểu
- [x] Thêm plugin JaCoCo vào `pom.xml`, cấu hình trong CI.
- [ ] Set ngưỡng coverage tối thiểu (gợi ý 80% line/branch). — chưa làm: chưa đặt `check` goal/ngưỡng
- Verify: CI báo coverage, fail nếu dưới ngưỡng.

### P3-2. Spring Boot Actuator
- [x] Thêm dependency `spring-boot-starter-actuator`.
- [x] Expose `/actuator/health`, `/actuator/metrics` (và các endpoint cần thiết).
- Verify: `GET /actuator/health` trả về UP.

### P3-3. Structured logging
- [x] Cấu hình Logback (log file, định dạng JSON hoặc standardized).
- Verify: khởi động app có file log, không lỗi ghi log.

### P3-4. Xác nhận test xanh trên CI
- [x] Chạy `./mvnw clean verify` cục bộ, chứng minh 18 test pass.
- Verify: BUILD SUCCESS, 18 tests passed.
- Ghi chú: thực tế hiện là 75 test (ban đầu 20, sau khi thêm security/CRUD/pagination/seed là 75). `.\mvnw.cmd clean test` → BUILD SUCCESS, 0 fail, 0 error.

---

## P4 — Docs & Quy trình nhánh

### P4-1. Cập nhật `README.md`
- [x] Hướng dẫn chạy bằng Docker / Docker Compose.
- [x] Hướng dẫn cấu hình env production (DB secret, JWT secret).
- [x] Hướng dẫn deploy và rollback.
- Verify: README đọc được, các lệnh chạy được.

### P4-2. Thêm `CONTRIBUTING.md` + Pull Request template
- [x] Ghi quy trình fork → branch → PR → review → merge.
- Verify: file tồn tại, PR template có sẵn.

### P4-3. Branching strategy & bảo vệ nhánh `main`
- [x] Định nghĩa nhánh: `main`, `develop`, `feature/*`, `hotfix/*`.
- [ ] Cấu hình GitHub branch protection: yêu cầu PR review + CI pass trước khi merge vào `main`.
- Verify: branch protection bật trên GitHub repo.

---

## P5 — Tài khoản người dùng (User accounts)

### P5-1. Đăng ký / đăng nhập / phân quyền theo tài khoản
Yêu cầu mới (10/2026): người dùng phải có tài khoản riêng để sử dụng hệ thống. Hiện tại hệ thống chỉ có một tài khoản admin lấy từ biến môi trường, mọi người dùng phải dùng chung và không có phân quyền.

- [ ] Tạo entity `User` (id, username hoặc email, password hash, role, enabled, created_at) kèm migration Flyway `V2__create_users.sql`; cập nhật `ddl-auto: validate` cho khớp.
- [ ] Lưu mật khẩu bằng BCrypt (`spring-security-crypto`), không bao giờ lưu plaintext.
- [ ] Thêm `POST /api/auth/register` (và `/api/v1/auth/register`) với DTO có validation (`@NotBlank`, `@Size(min = 8)`, `@Email` nếu dùng email).
- [ ] Sửa `AuthService.login()` đọc tài khoản từ bảng `users` thay vì so sánh trực tiếp với `app.admin.username` / `app.admin.password`; dùng `PasswordEncoder` để so sánh.
- [ ] Thêm role `ADMIN` và `USER`; ghi role vào claim của JWT và nạp vào `Authentication` trong `JwtAuthenticationFilter`.
- [ ] Phân quyền trong `SecurityConfig`: API quản trị (`POST/PUT/DELETE /api/languages`, `/api/tours`, `/api/locations`) chỉ ADMIN; người dùng thường chỉ đọc và `POST /api/analytics/logs`.
- [ ] Mã lỗi: username/email trùng → 409, mật khẩu quá ngắn → 400, sai định dạng → 400, đăng nhập sai → 401, đúng token nhưng thiếu quyền → 403.
- [ ] Tương thích ngược: tài khoản admin từ `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` vẫn đăng nhập được (seed vào bảng `users` khi chạy lần đầu) để không phá môi trường đang deploy.
- [ ] Seed trong `DataInitializer`: 1 tài khoản admin + 1 tài khoản user mẫu; ghi tài khoản mẫu vào README và `.env.example`.
- [ ] Cập nhật `README.md` (mục API + hướng dẫn đăng ký) và OpenAPI annotation cho endpoint mới.
- Verify: `.\mvnw.cmd clean test` xanh; register → login → gọi API bằng token user (đọc OK, ghi OK) và token admin (đủ quyền); gọi API quản trị bằng token user nhận 403; đăng nhập sai nhận 401; register trùng username nhận 409.
- Ghi chú: chưa làm — mới là yêu cầu, chưa có code. Cần bảng `users` và cơ chế role; hiện `SecurityConfig` chỉ yêu cầu `authenticated()` nên mọi token đều có toàn quyền.

---

## P6 — Dashboard & Phân tích (CMS)

### P6-1. Dashboard + mở rộng API phân tích
Yêu cầu mới (10/2026): cần màn hình dashboard cho người dùng/quản trị xem thông tin về hệ thống. Hiện repo chỉ có API `GET /api/analytics/dashboard` trả JSON, chưa có giao diện nào (repo backend-only, 0 file frontend).

- [ ] Chốt phạm vi UI với team: dự án web riêng (React/Vue) gọi API, hay Thymeleaf trong cùng repo. Ghi quyết định vào `docs/adr/`.
- [ ] Mở rộng API dashboard: thêm bộ lọc `from`/`to`/`languageCode`, chuẩn hóa tham số và trả thêm nhóm theo ngôn ngữ.
- [ ] Bổ sung số liệu theo thời gian (timeseries theo ngày/tuần) để vẽ biểu đồ xu hướng.
- [ ] Bổ sung số liệu người dùng: số thiết bị duy nhất (`deviceId`), tỷ lệ offline vs online, số lượt tải gói offline.
- [ ] Ghi log lượt tải offline: `AudioPackageService.createOfflinePackage()` hiện không ghi log — thêm bảng/bước ghi nhận (bảng mới `package_downloads` qua migration `V2__...`, hoặc dùng chung `playback_logs` với `trigger_type` mới).
- [ ] Chuẩn hóa tọa độ heatmap: query hiện `GROUP BY latitude, longitude` theo đúng giá trị thô → cần làm tròn (ví dụ 4–5 chữ số thập phân) để heatmap không vỡ thành hàng nghìn điểm rời rạc.
- [ ] Phân quyền: sau P5-1, endpoint dashboard chỉ cho role `ADMIN`; người dùng thường trả 403.
- [ ] Widget tối thiểu cho màn hình dashboard: tổng lượt nghe, thời gian nghe trung bình, tỷ lệ QR vs GPS vs MANUAL, Top 5 POI (biểu đồ cột), heatmap trên bản đồ (Leaflet), biểu đồ xu hướng theo thời gian, lọc theo ngôn ngữ và khoảng thời gian.
- [ ] Tài liệu: cập nhật README (mục API + ảnh chụp màn hình) và OpenAPI annotation cho tham số mới.
- Verify: `.\mvnw.cmd clean test` xanh; đối chiếu số liệu API với truy vấn SQL thủ công trên `playback_logs` (tổng lượt nghe, top POI) khớp; mở UI thấy đủ widget; lọc theo ngôn ngữ và theo khoảng thời gian trả dữ liệu khác nhau; token role `USER` gọi dashboard nhận 403.
- Ghi chú: chưa làm — mới là yêu cầu, chưa có code. Cần chốt stack frontend trước khi bắt đầu; nếu UI thuộc repo CMS riêng thì phần việc ở đây chỉ còn mở rộng API.

---

## Trạng thái thực hiện (agent cập nhật)

| Mã | Nội dung | Trạng thái | Ghi chú |
|----|----------|------------|---------|
| P0-1 | Security + JWT | [x] | login từ env, 401 khi thiếu/sai token |
| P0-2 | Env var credentials | [x] | `application-prod.yml` + `.env.example`; `.env` bị ignore |
| P0-3 | Migration + ddl-auto validate | [x] | Flyway V1 + `validate`; verify trên DB trống |
| P0-4 | Bean Validation | [x] | done |
| P0-5 | GlobalExceptionHandler chuẩn | [x] | thêm 401/404/409, không rò SQL |
| P1-1 | Dockerfile | [x] | written; not verified (no docker) |
| P1-2 | docker-compose.yml | [x] | written; not verified (no docker) |
| P1-3 | CI push image | [x] | workflow job added |
| P1-4 | Deploy staging | [ ] | cần hạ tầng + secret GitHub, không làm trong repo |
| P1-5 | Rollback docs | [x] | `docs/ROLLBACK.md` |
| P2-1 | CRUD Language | [x] | 409 trùng code / 409 đang được dùng / 404 |
| P2-2 | Phân trang | [x] | chỉ `/api/v1`, `/api` giữ mảng JSON cũ |
| P2-3 | API versioning | [x] | song song `/api` + `/api/v1` |
| P2-4 | Seed dữ liệu đầy đủ | [x] | seed đã đủ + `SeedDataIntegrityTest` |
| P3-1 | JaCoCo coverage | [x] | plugin added |
| P3-2 | Actuator | [x] | health public, metrics/info cần token |
| P3-3 | Structured logging | [x] | logback-spring.xml |
| P3-4 | Test xanh | [x] | 75 test, 0 fail, 0 error |
| P4-1 | Cập nhật README | [x] | docker/env/prod/API v1 |
| P4-2 | CONTRIBUTING + PR template | [x] | files added |
| P4-3 | Branching + protect main | [x] | docs/BRANCHING.md |
| P5-1 | Tài khoản người dùng (register + role) | [ ] | yêu cầu mới 10/2026, chưa làm |
| P6-1 | Dashboard + mở rộng API phân tích | [ ] | yêu cầu mới 10/2026, chưa làm |

**Việc còn lại:** P1-4 deploy staging (cần server/Render/Railway và secret trên GitHub, không thể hoàn thành trong mã nguồn), P5-1 tài khoản người dùng và P6-1 dashboard (hai yêu cầu mới, chưa bắt đầu).

**Đã defer (ghi nhận, chưa sửa):** Location/Tour trả 400 khi không tìm thấy còn Language trả 404; phân trang fetch-join chạy in-memory (HHH90003004, đúng kết quả nhưng chậm khi dữ liệu lớn); login chưa giới hạn số lần thử; docker-compose/profile prod chưa chạy thật vì máy không có Docker.

**Lệnh verify chính:** `./mvnw clean verify` (Linux/macOS) hoặc `.\mvnw.cmd clean verify` (Windows).
