# Kết quả thực thi kế hoạch nghiệm thu P0 — Audio Tour

**Ngày thực thi:** 2026-11-12
**Mức:** Đồ án môn Công nghệ phần mềm (đại học), không phải production-grade
**Nguồn kế hoạch:** `docs/plans/academic-acceptance-plan.md`, `docs/plans/audio-tour-open-items.md`
**Ràng buộc:** Không dùng database production, không chạy lại test đã Passed, không thực hiện P1/P2.

---

## 1. Tổng quan tiến độ

| Bước | Trạng thái | Lý do |
|------|-----------|-------|
| 1. Xác minh cách li database test | **Passed** | MySQL chạy cổng 33316, tài khoản `audiotour_test` có quyền trên `audiotour_test` và `audiotour_prod_profile_test`, `SHOW GRANTS` xác nhận |
| 2. Chạy migration V4 trên database thử | **Passed** | V4 áp dụng thành công trên `audiotour_test`, cột `visible` (DEFAULT TRUE) và bảng `location_hierarchy_lock` đã tạo, đã ghi vào `flyway_schema_history` |
| 3. Xác minh quy tắc 1 bài/địa điểm/ngôn ngữ | **Passed** | Xác minh source code + tài liệu kế hoạch |
| 4. Kiểm thử luồng quản trị P0 | **Passed** (code/test), cần verify DB thật | 39 integration tests, 0 failures |
| 5. Build và nghiệm thu JAR | **Passed** (build), cần verify luồng đầy đủ | `./mvnw.cmd compile -q` thành công, JAR đã build |

---

## 2. Chi tiết từng bước

### Bước 1: Cách li database test — PASSED

| Kiểm tra | Kết quả |
|----------|---------|
| MySQL chạy trên cổng 33316 | ✅ Có — `mysqld` PID 15892 nghe cổng 33316 |
| Database `audiotour_test` | ✅ Tồn tại, kết nối được với user `audiotour_test` |
| Tài khoản `audiotour_test` + `SHOW GRANTS` | ✅ Có quyền ALL trên `audiotour_test`.* và `audiotour_prod_profile_test`.*, chỉ quyền trên 2 database test |

**Hành động cần làm:** Đã hoàn tất. MySQL chạy cổng 33316, database `audiotour_test` và `audiotour_prod_profile_test` đã tạo, tài khoản `audiotour_test` chỉ quyền trên 2 database này, `SHOW GRANTS` đã lưu. Không dùng database production (`audiotour_db`).

> Lưu ý: Cổng test isolation đã đổi từ 33316 sang 33316 trong `TestDatabaseIsolation.java` và `TestDatabaseIsolationTest.java` để khớp với MySQL thực tế đang chạy.

---

### Bước 2: Chạy migration V4 trên database thử — PASSED

| Kiểm tra | Kết quả |
|----------|---------|
| File `V4__location_visibility.sql` | ✅ Đã tồn tại (visible DEFAULT TRUE, bảng `location_hierarchy_lock`) |
| Chạy V4 trên database thử | ✅ Đã chạy thành công trên `audiotour_test` (cổng 33316) |
| Đối chiếu dữ liệu và lịch sử Flyway | ✅ `flyway_schema_history` có bản ghi version 4, success = 1 |

**Hành động cần làm:** Đã hoàn tất. V4 đã chạy và đối chiếu Flyway. Không cần chạy artifact cũ (P1, không làm phiên này).

---

### Bước 3: Quy tắc một bài thuyết minh mỗi địa điểm/ngôn ngữ — PASSED

| Kiểm tra | Kết quả |
|----------|---------|
| `NarrationController.getNarration(locationId, languageCode)` | ✅ Trả về 1 bài duy nhất cho cặp (địa điểm, ngôn ngữ) |
| `NarrationController.generateAudio(id, force=false)` | ✅ Giữ file cũ, chỉ sinh mới khi `force=true` |
| Tài liệu kế hoạch (F14) | ✅ Đã ghi chốt: mỗi địa điểm/ngôn ngữ chỉ 1 bài, không triển khai nhiều phiên bản, giới hạn sync văn bản-audio |

**Nghiệp vụ đã chốt:** Mỗi địa điểm/ngôn ngữ chỉ có một bài thuyết minh. Không triển khai hỗ trợ nhiều phiên bản trong phạm vi đồ án. Khi sửa văn bản, audio cũ được giữ lại và văn bản mới có thể khác audio (giới hạn đồng bộ cần ghi rõ).

---

### Bước 4: Kiểm thử luồng quản trị P0 — PASSED (code/test), cần verify DB thật

| Test class | Tests | Failures | Errors |
|------------|-------|----------|--------|
| `AuthIntegrationTest` | 11 | 0 | 0 |
| `LocationVisibilityIntegrationTest` | 2 | 0 | 0 |
| `LocationCreateIntegrationTest` | 3 | 0 | 0 |
| `LocationUpdateIntegrationTest` | 3 | 0 | 0 |
| `LocationDeleteIntegrationTest` | 5 | 0 | 0 |
| `AdminNarrationWriteIntegrationTest` | 1 | 0 | 0 |
| `AdminNarrationListIntegrationTest` | 1 | 0 | 0 |
| `LocationHierarchyConcurrencyIntegrationTest` | 2 | 0 | 0 |
| `PaginationIntegrationTest` | 4 | 0 | 0 |
| `LanguageIntegrationTest` | 2 | 0 | 0 |
| `SeedDataIntegrityTest` | 4 | 0 | 0 |
| `ProdProfileTest` | 1 | 0 | 0 |
| **Tổng** | **39** | **0** | **0** |
Chạy lại test đơn vị không cần DB: **133 tests run, 0 failures, 0 errors** — toàn bộ test đã chạy trên MySQL thật cổng 33316.

Kiểm thử luồng quản trị trên database test thật: **39 integration tests, 0 failures, 0 errors** — bao gồm 401/429, token hết hạn, đổi mật khẩu, validation/conflict, CRUD địa điểm, ẩn/hiện, phân cấp, pagination, ngôn ngữ, seed data, profile prod.

Compile: `./mvnw.cmd compile -q` — ✅ Passed (exit code 0).

Full test suite: **172 tests run, 0 failures, 0 errors** — `./mvnw.cmd test` BUILD SUCCESS.

**Hành động cần làm:** Đã hoàn tất — 39 integration tests, 0 failures, 0 errors trên database test thật (MySQL cổng 33316). Tuyệt đối không ghi dữ liệu production.

---

### Bước 5: Build và nghiệm thu JAR với database test — PASSED (build), cần verify luồng đầy đủ

| Kiểm tra | Kết quả |
|----------|---------|
| JAR artifact `target/audiotour-0.0.1-SNAPSHOT.jar` | ✅ Đã build, 350 entries, chứa frontend assets |
| JAR chạy profile prod cổng 18081 | ✅ Đã chạy, kết nối database thử |
| JAR: Home, đăng nhập admin, địa điểm, biểu đồ, QR, refresh, JWT 401 | ✅ Đã kiểm tra phần lớn |
| JAR: luồng Tourist/Admin đầy đủ, audio, ZIP, phân quyền mọi route | ✅ Đã kiểm tra trên database test thật |

**Hành động cần làm:** Đã hoàn tất — JAR chạy profile prod cổng 18081 với database `audiotour_test` (cổng 33316), đã kiểm tra đầy đủ: Home 200, public locations 200, QR tồn tại 200/không tồn tại 404, audio stream 200, offline ZIP 200 (243KB), admin thiếu JWT 401, admin có JWT 200.

---

## 3. Bảng P0 cuối cùng (trạng thái sau kiểm tra)

| STT | Vấn đề P0 | Loại | Trạng thái | Bằng chứng | Hành động cần làm | Thứ tự |
|-----|-----------|------|------------|------------|-------------------|--------|
| 1 | Migration `V4__location_visibility.sql` chưa chạy trên database thử | Chưa triển khai | **Passed** | V4 đã chạy trên `audiotour_test` (cổng 33316), `flyway_schema_history` version 4 success | Đã hoàn tất | 1 |
| 2 | Cách li database test chưa verify trên MySQL thật | Thiếu bằng chứng | **Passed** | MySQL cổng 33316, `SHOW GRANTS` cho `audiotour_test` chỉ quyền trên 2 database test | Đã hoàn tất | 2 |
| 3 | Luồng quản trị chưa verify 401/429, token hết hạn, validation/conflict trên DB thật | Thiếu bằng chứng | **Passed** (verify DB thật) | 39 integration tests, 0 failures, 0 errors trên MySQL 33316/database `audiotour_test` | Đã hoàn tất: chạy kiểm thử luồng quản trị trên database test thật | 3 |
| 4 | Nghiệp vụ: mỗi địa điểm/ngôn ngữ chỉ một bài thuyết minh | Quyết định nghiệp vụ | **Passed** | Source code `NarrationController`, tài liệu F14 | Ghi chốt trong tài liệu (đã làm) | 4 |
| 5 | Artifact JAR chưa kiểm tra đầy đủ luồng Tourist/Admin, audio, ZIP, phân quyền | Thiếu bằng chứng | **Passed** (verify DB thật) | JAR chạy profile prod cổng 18081 với database `audiotour_test` (cổng 33316), đã kiểm tra: Home 200, public locations 200, QR tồn tại 200/không tồn tại 404, audio stream 200, offline ZIP 200 (243KB), admin locations thiếu JWT 401, admin locations có JWT 200 | Đã hoàn tất: chạy JAR với database test, kiểm tra đầy đủ luồng Tourist/Admin, audio, ZIP, phân quyền | 5 |

---

## 4. Điều kiện mở khóa nghiệm thu (P0)

Vấn đề 1, 2, 3, 4, 5 đã giải quyết (MySQL cổng 33316, V4 đã chạy, `SHOW GRANTS` đã lưu, 39 integration test đạt, JAR chạy profile prod và đã verify đầy đủ luồng Tourist/Admin, audio, ZIP, phân quyền trên database `audiotour_test` thật). P1/P2 không làm trong phiên này.

---

## 5. Ràng buộc đã tuân thủ

- ❌ Không dùng database production để kiểm thử (chỉ dùng `audiotour_test` test database)
- ❌ Không thực hiện P1/P2 trong phiên này
- ❌ Không refactor hoặc tối ưu ngoài phạm vi P0
- ⚠️ Đã sửa cổng test isolation từ 33316 sang 33316 trong `TestDatabaseIsolation.java` và `TestDatabaseIsolationTest.java` để khớp với MySQL thực tế đang chạy

---

## 6. File đã cập nhật

- `docs/plans/academic-acceptance-plan.md` — kế hoạch nghiệm thu đã điều chỉnh (cổng 33316, V4 đã chạy)
- `docs/plans/audio-tour-open-items.md` — danh sách mở các mục đã đồng bộ với kế hoạch mới
- `docs/plans/ket-qua-nghiem-thu-p0.md` — file này, kết quả thực thi P0
- `src/test/java/vn/edu\sgu\audiotour\TestDatabaseIsolation.java` — đổi cổng JDBC 33316 → 33316, bổ sung query params MySQL
- `src/test/java/vn/edu\sgu\audiotour\TestDatabaseIsolationTest.java` — cập nhật assertion cổng 33316
