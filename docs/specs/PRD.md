# PRD — Hệ thống Thuyết minh Audio Tour Tự Động Đa Ngôn Ngữ

| Mục | Nội dung |
| :--- | :--- |
| Mã tài liệu | PRD-AUDIOTOUR-001 |
| Phiên bản | 1.1 |
| Ngày | 20/10/2026 |
| Trạng thái | Đã bàn giao (as-built) |
| Dự án | Multi Languages Automatic Translation — Đồ án môn Công nghệ Phần mềm, ĐH Sài Gòn (SGU) |
| Phạm vi | Backend REST API 3 tầp (Java 21 / Spring Boot 4.1.1 / MySQL 8) |
| Nguồn đối chiếu | Mã nguồn `src/main/java`, `src/test/java`, `docs/USE_CASES.md` (UC-01…UC-08), `docs/PID.md`; kiểm chứng runtime với MySQL 8 local ngày 20/10/2026 |

> Phiên bản 1.1 đối chiếu lại toàn bộ yêu cầu với mã nguồn hiện tại và bám theo đặc tả ca sử dụng `docs/USE_CASES.md`.
> Mọi mã trạng thái HTTP trong tài liệu này là **hành vi thực tế** của mã nguồn, không phải thiết kế mong muốn.

---

## 1. Bối cảnh & vấn đề

Du khách tham quan TP. Hồ Chí Minh tại các điểm di tích (Dinh Độc Lập) và trạm dừng xe buýt lịch sử (Khánh Hội, Vĩnh Hội, Xóm Chiếu — Quận 4) cần nghe thuyết minh bằng ngôn ngữ bản thân. Tài liệu thuyết minh in ấn bị giới hạn, không đa ngôn ngữ, và không dùng được khi mạng yếu.

Hệ thống cung cấp API để Mobile App tự động phát thuyết minh khi du khách đến nơi, theo hai kích hoạt song song: **quét mã QR** và **định vị GPS geofence**. Nội dung audio được tải về máy dưới dạng gói ZIP để nghe offline.

Phạm vi tài liệu này: **toàn bộ backend**. Mobile App và Web CMS là hệ thống tiêu thụ, nằm ngoài phạm vi.

---

## 2. Mục tiêu

| # | Mục tiêu | Chỉ số thành công |
| :--- | :--- | :--- |
| G1 | Du khách nghe được thuyết minh tại điểm tham quan mà không cần mạng | 100% gói offline tải lại được sau khi đồng bộ |
| G2 | Tự động hóa kích hoạt thuyết minh khi du khách đến nơi | ≥ 80% lượt nghe kích hoạt bằng QR hoặc geofence, không cần thao tác tay |
| G3 | Đa ngôn ngữ cho du khách nước ngoài | Danh mục có đủ `vi` + `en`; mọi POI bàn giao có ≥ 1 ngôn ngữ (dữ liệu seed: 6/6 POI có `vi`, 1/6 POI có `en`, 1/6 POI có `zh` — bản dịch mẫu để thử TTS) |
| G4 | Cung cấp dữ liệu vận hành cho CMS | Dashboard trả về Top POI, tỷ lệ QR/GPS, heatmap, thời gian nghe trung bình |
| G5 | Bàn giao API ổn định cho team Mobile | Tài liệu OpenAPI 3 sinh tự động + mọi nhóm endpoint (auth, locations, packages, analytics, tours, languages) có kiểm thử tự động |

### 2.1. Ngoài phạm vi (Out of scope)

- Ứng dụng di động (Android/iOS) — nơi tiêu thụ API.
- Giao diện Web CMS — chỉ cung cấp API Dashboard.
- Cơ chế dịch máy tự động (MT) giữa các ngôn ngữ. Văn bản thuyết minh vẫn biên soạn thủ công; riêng file âm thanh có thể sinh tự động từ văn bản bằng TTS (ADR 0004). Bước tiếp theo đã có đặc tả và quyết định: `docs/specs/feature-them-ngon-ngu.md`, ADR 0005 (chưa triển khai).
- Thanh toán, đặt vé, đánh giá, bình luận.
- Quản lý người dùng nhiều tài khoản / phân quyền RBAC.

---

## 3. Người dùng & nhu cầu

Tác nhân lấy theo `docs/USE_CASES.md` §2, gộp thêm nhóm vận hành nội bộ.

| Persona / Tác nhân | Loại | Use Case liên quan | Nhu cầu cốt lõi |
| :--- | :--- | :--- | :--- |
| **Du khách (Tourist)** | Primary | UC-01, UC-03, UC-04, UC-07, UC-08 | Biết mình đang ở đâu, nghe được thuyết minh ngay, tải trước để nghe offline, chọn ngôn ngữ |
| **Thiết bị / Cảm biến GPS** | System | UC-02, UC-05 | Tự đối chiếu tọa độ với danh sách POI đã tải về và phát tín hiệu kích hoạt âm thanh |
| **Ban Quản lý (Admin / Tour Operator)** | Primary | UC-06, UC-07 | Xem được POI nào hot, kênh kích hoạt nào hiệu quả, du khách nghe bao nhiêu |
| **Đội vận hành (DevOps)** | Nội bộ | — | App khởi động được với cấu hình production rõ ràng, secret không lộ, test tự động chạy mỗi commit |

---

## 4. Yêu cầu chức năng

Cột **UC** trỏ tới mã ca sử dụng trong `docs/USE_CASES.md`.

### 4.1. Xác thực (FR-AUTH)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-AUTH-01 | Đăng nhập bằng username/password, trả về JWT | So khớp với tài khoản quản trị cấu hình qua biến môi trường `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` | — |
| FR-AUTH-02 | Token ký HS256, hạn 24h (mặc định) | `JWT_SECRET` ≥ 32 byte, `JWT_EXPIRATION_MS` cấu hình được | — |
| FR-AUTH-03 | Mọi endpoint `/api/**` và `/api/v1/**` yêu cầu `Authorization: Bearer <token>` | Ngoại lệ: `POST /api/auth/login`, `POST /api/v1/auth/login`, Swagger UI, `/v3/api-docs/**`, `/actuator/health`, `/actuator/health/**` | — |
| FR-AUTH-04 | Thiếu token / token sai / token hết hạn → **401** (không phải 403) | `authenticationEntryPoint` gọi `sendError(SC_UNAUTHORIZED)`; **body 401 theo format lỗi mặc định của Spring Boot** (`timestamp/status/error/path`, không có `message`) — khác format 5 trường ở §7.2 | — |
| FR-AUTH-05 | Fail-fast lúc khởi động khi secret hoặc tài khoản cấu hình sai | App không boot với `JWT_SECRET` rỗng/ngắn hơn 32 byte, username rỗng/ngắn hơn 3 ký tự, hoặc mật khẩu rỗng | — |

### 4.2. Điểm quan tâm & cấu trúc POI (FR-LOC)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-LOC-01 | Lấy cây địa điểm gốc kèm khu vực con | Chỉ trả địa điểm không có cha (`parent_id IS NULL`); mỗi node kèm mảng `subLocations` (1 cấp) | UC-01 |
| FR-LOC-02 | Lấy danh sách POI đầy đủ cho đồng bộ offline | Trả tọa độ, `radiusInMeters`, `priority` để App lưu SQLite và tự so sánh vị trí | UC-01, UC-02 |
| FR-LOC-03 | Lấy chi tiết 1 địa điểm theo ID | ID không tồn tại → **400** (`error` = `Bad Request / Not Found`), vì `LocationService` ném `RuntimeException` | UC-03 |
| FR-LOC-04 | Tra cứu địa điểm theo mã QR | `qr_code` là duy nhất toàn hệ thống; dùng khi du khách quét mã không cần GPS; không tìm thấy → **400**. ⚠️ **Hiện chưa chạy được** — xem B-13 | UC-03 |
| FR-LOC-05 | Lấy danh sách khu vực con của 1 địa điểm | Ví dụ: các phòng ban bên trong Dinh Độc Lập; ID cha không tồn tại → **400**. ⚠️ **Hiện chưa chạy được** — xem B-13 | UC-08 |
| FR-LOC-06 | Cấu trúc cha–con, cho phép lồng nhiều cấp | `locations.parent_id` tự tham chiếu | UC-08 |

### 4.3. Gói nội dung offline (FR-PKG)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-PKG-01 | Đóng gói thuyết minh của 1 POI theo 1 ngôn ngữ thành file ZIP | ZIP chứa `content.txt` (UTF-8, nội dung văn bản) + file MP3 thật lấy từ `uploads/audio/<audioUrl>`; tên entry audio là **tên file gốc** (vd `dinh-doc-lap-vi.mp3`) | UC-04 |
| FR-PKG-02 | Trả file ZIP trực tiếp với header tải về | `Content-Type: application/zip`, `Content-Disposition: attachment; filename="tour-offline.zip"` | UC-04 |
| FR-PKG-03 | Lỗi phải nêu rõ nguyên nhân | 3 trường hợp, tất cả trả **400**: không có bản thuyết minh cho POI+ngôn ngữ đó; `audioUrl` rỗng; file MP3 không tồn tại trên ổ đĩa. Không có chế độ "fallback" — thiếu file MP3 là lỗi, không phải gói chỉ có text | UC-04 |
| FR-PKG-04 | Gói tải được dùng khi mất mạng hoàn toàn | Không phụ thuộc kết nối sau khi tải; App lưu file ZIP và giải nén cục bộ | UC-04 |

### 4.4. Phân tích dữ liệu (FR-ANA)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-ANA-01 | Ghi log lượt nghe từ App | Bắt buộc: `locationId`, `languageCode`, `listenDurationSeconds ≥ 0`, `triggerType` | UC-05 |
| FR-ANA-02 | Tự điền giá trị mặc định khi App thiếu trường | `triggerType` → `QR_CODE` (mặc định ở DTO); `latitude`/`longitude` → tọa độ của POI (mặc định ở Service). `languageCode` và `listenDurationSeconds` **không có mặc định ở tầng API** — thiếu/rỗng/âm trả **400** ở bước validate (`@NotBlank`, `@NotNull`, `@PositiveOrZero`) | UC-05 |
| FR-ANA-03 | Dashboard tổng quan | Trả: tổng lượt nghe, thời gian nghe trung bình (làm tròn 1 chữ số thập phân), đếm theo `triggerType`, Top 5 POI, tập tọa độ heatmap | UC-06 |
| FR-ANA-04 | Dữ liệu lịch sử ẩn danh | `deviceId` là UUID tự sinh ở App, không định danh du khách; không lưu tên, SĐT, email | UC-05, UC-06 |
| FR-ANA-05 | Heatmap dùng tọa độ lúc bắt đầu nghe | `intensity` = số lượt nghe gom theo tọa độ; bỏ qua bản ghi thiếu tọa độ | UC-06 |

### 4.5. Tuyến tour (FR-TOUR)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-TOUR-01 | Lấy danh sách tour kèm lộ trình | Quan hệ n-n với `locations` qua bảng trung gian `tour_locations` | UC-07 |
| FR-TOUR-02 | Lấy chi tiết 1 tour theo ID | Kèm danh sách điểm dừng; ID không tồn tại → **400** | UC-07 |
| FR-TOUR-03 | Tour có thời lượng ước tính | `estimatedDurationMinutes` để App ước lượng lịch trình | UC-07 |

### 4.6. Quản lý ngôn ngữ (FR-LANG)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-LANG-01 | CRUD danh mục ngôn ngữ | `code` duy nhất, tối đa 10 ký tự; `name` bắt buộc (tối đa 50 ký tự) | — |
| FR-LANG-02 | Trùng `code` khi tạo → 409 | Pre-check trước khi ghi; race condition thì `DataIntegrityViolationException` cũng được dịch thành 409 | — |
| FR-LANG-03 | Cập nhật `code` đã thuộc ngôn ngữ khác → 409 | Không cho tự trùng chính nó | — |
| FR-LANG-04 | Xóa ngôn ngữ đang được thuyết minh tham chiếu → 409 | Chặn xóa nếu `narrations` còn bản ghi dùng ngôn ngữ đó | — |
| FR-LANG-05 | Xóa thành công → 204 No Content | Ngôn ngữ không tồn tại → **404** (đây là nhóm duy nhất dùng `NotFoundException`) | — |

### 4.7. Phiên bản API (FR-VER)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-VER-01 | Nhóm `/api/...` giữ định dạng mảng JSON phẳng | Không phá client cũ | UC-01, UC-03, UC-08 |
| FR-VER-02 | Nhóm `/api/v1/...` trả dạng phân trang | `{ content, totalElements, totalPages, size, number, ... }`, hỗ trợ `page`, `size`, `sort`; mặc định `size=20` | — |
| FR-VER-03 | Tài liệu OpenAPI 3 + Swagger UI tự sinh | `http://localhost:8080/swagger-ui.html` | — |

### 4.8. Sinh audio thuyết minh (FR-TTS)

| ID | Yêu cầu | Quy tắc nghiệp vụ | UC |
| :--- | :--- | :--- | :--- |
| FR-TTS-01 | Sinh file MP3 cho 1 bài thuyết minh từ `textContent` | `POST /api/narrations/{id}/audio` (token admin) ghi file vào `uploads/audio` và lưu tên file vào `audioUrl`; `durationInSeconds` ước lượng `length/15` | — |
| FR-TTS-02 | Tái sử dụng file đã sinh | File đã tồn tại thì trả lại ngay, **không** gọi nhà cung cấp TTS, không tốn credit; `force=true` mới sinh lại | — |
| FR-TTS-03 | Lỗi TTS không làm hỏng luồng audio | Ghi file tạm rồi move; thiếu `ELEVENLABS_API_KEY` hoặc upstream lỗi trả **503** kèm nguyên nhân, app vẫn khởi động bình thường | — |

---

## 5. Mô hình dữ liệu

### 5.1. Thực thể chính

```
languages (id, code*, name)
    │ 1
    │
    │ N
narrations (id, location_id, language_id, text_content, audio_url, duration_in_seconds)
    │ N
    │
    │ 1
locations (id, name, address, latitude, longitude, radius_in_meters,
           priority, qr_code*, image_url, parent_id ──► locations)
    │ 1                    │
    │ N                    │ N
playback_logs              tour_locations ──► tours (id, name, description,
(id, location_id, language_code,                estimated_duration_minutes, image_url)
 listen_duration_seconds, trigger_type,
 latitude, longitude, device_id, created_at)
```

`*` = ràng buộc UNIQUE. `parent_id` và `tour_locations` là quan hệ tự tham chiếu / n-n.

### 5.2. Quy tắc dữ liệu

| Bảng | Quy tắc |
| :--- | :--- |
| `locations` | `qr_code` UNIQUE; `radius_in_meters` và `priority` có giá trị mặc định ở tầng Java (30.0 / 1), cột DB cho phép NULL |
| `playback_logs` | `trigger_type` ENUM `QR_CODE` / `GEOFENCE` / `MANUAL`; `created_at` NOT NULL |
| `languages` | `code` UNIQUE, VARCHAR(10) |
| Tất cả bảng | InnoDB, charset `utf8mb4`, collation `utf8mb4_unicode_ci` |

### 5.3. Nguồn dữ liệu mẫu

`DataInitializer` chạy 1 lần khi bảng `languages` còn rỗng, tạo sẵn: 2 ngôn ngữ (`vi`, `en`), 6 địa điểm (3 trạm xe buýt Quận 4 + Dinh Độc Lập + 2 phòng ban con), 7 bản thuyết minh có file MP3 thật trong `uploads/audio/`, 2 tuyến tour, 7 log mẫu cho dashboard.

> Độ phủ ngôn ngữ của dữ liệu seed: 6/6 POI có `vi`; chỉ Phòng Khánh Tiết có thêm `en`. Xem §10.

---

## 6. Yêu cầu phi chức năng

### 6.1. Bảo mật (NFR-SEC)

| ID | Yêu cầu |
| :--- | :--- |
| NFR-SEC-01 | Mọi endpoint nghiệp vụ bắt buộc xác thực; endpoint không xác định trong danh sách cho phép → `403` (`denyAll`) |
| NFR-SEC-02 | Profile `prod` **không** có giá trị mặc định cho DB/JWT/admin → app không boot nếu thiếu biến bắt buộc |
| NFR-SEC-03 | `docker-compose.yml` từ chối khởi động khi thiếu secret hoặc tài khoản (cú pháp `${VAR:?message}`) |
| NFR-SEC-04 | Lỗi ràng buộc DB không lộ message SQL thô ra ngoài; trả thông điệp chung |
| NFR-SEC-05 | API stateless, không CSRF, không httpBasic, không formLogin, không session |
| NFR-SEC-06 | Dữ liệu log nghe ẩn danh: không thu thập định danh cá nhân |

### 6.2. Hiệu năng (NFR-PERF)

| ID | Yêu cầu |
| :--- | :--- |
| NFR-PERF-01 | `open-in-view: false` — không mở session Hibernate qua view, tránh N+1 và rò rỉ connection |
| NFR-PERF-02 | Truy vấn dashboard tổng hợp bằng query đếm/tính trung bình ở tầng DB, không load toàn bộ bảng log vào RAM |
| NFR-PERF-03 | Gói offline nén trực tiếp vào `ByteArrayOutputStream`, không ghi file tạm trên đĩa |
| NFR-PERF-04 | Endpoint đọc danh sách lớn (`/api/locations/pois`) chỉ dùng cho đồng bộ offline, không dùng cho duyệt UI |
| NFR-PERF-05 | Quan hệ trả về cho client phải được fetch sẵn bằng `@EntityGraph` (`subLocations`, `locations`) để không lỗi `LazyInitializationException` khi `open-in-view: false` |

### 6.3. Khả dụng & vận hành (NFR-OPS)

| ID | Yêu cầu |
| :--- | :--- |
| NFR-OPS-01 | Actuator: expose `health`, `metrics`, `info`; chỉ `health` public, `metrics`/`info` yêu cầu token |
| NFR-OPS-02 | Kiểm thử tự động: unit test tầng Service, test tầng Controller, integration test với MySQL — hiện có **18 lớp test / 75 phương thức kiểm thử** |
| NFR-OPS-03 | CI chạy mỗi commit/PR: build → test → đóng gói JAR → lưu Artifact (GitHub Actions), kèm build & push Docker image khi push |
| NFR-OPS-04 | Migration schema bằng Flyway; `ddl-auto: validate` để chặn lệch schema |
| NFR-OPS-05 | Quy trình rollback được mô tả bằng văn bản (`docs/ROLLBACK.md`) |
| NFR-OPS-06 | Có môi trường dev (mặc định trong `application.yml`) tách biệt hoàn toàn với prod (`application-prod.yml`) |

### 6.4. Tài liệu & khả năng kiểm thử (NFR-DOC)

| ID | Yêu cầu |
| :--- | :--- |
| NFR-DOC-01 | Mọi endpoint có mô tả tiếng Việt trên Swagger UI, có ví dụ request/response |
| NFR-DOC-02 | 100% service có unit test; controller được kiểm thử trực tiếp — `AnalyticsController`, `AudioPackageController`, `AuthController`, `LanguageController`, `LocationController`. `TourController` mới chỉ được kiểm thử gián tiếp qua `TourServiceTest` |
| NFR-DOC-03 | Kiểm thử contract: kết quả API thực tế khớp mô tả trong tài liệu này — hiện có 2 endpoint lệch, xem B-13 |

---

## 7. Hợp đồng API

### 7.1. Danh mục endpoint

| Nhóm | UC | Method | Đường dẫn | Mô tả |
| :--- | :--- | :---: | :--- | :--- |
| **Xác thực** | — | POST | `/api/auth/login`, `/api/v1/auth/login` | Đăng nhập → `{ token, tokenType }` |
| **POI** | UC-01 | GET | `/api/locations` | Địa điểm gốc kèm khu vực con (mảng phẳng) |
| | — | GET | `/api/v1/locations` | Bản phân trang (`page`, `size`, `sort`) |
| | UC-01, UC-02 | GET | `/api/locations/pois` | Đồng bộ toàn bộ POI (tọa độ, bán kính, ưu tiên) cho SQLite offline |
| | UC-03 | GET | `/api/locations/{id}` | Chi tiết 1 địa điểm (kèm khu vực con) |
| | UC-08 | GET | `/api/locations/{id}/sub-locations` | Khu vực con của 1 địa điểm ⚠️ lỗi runtime (B-13) |
| | UC-03 | GET | `/api/locations/by-qr/{qrCode}` | Tra cứu địa điểm theo mã QR ⚠️ lỗi runtime (B-13) |
| **Gói offline** | UC-04 | GET | `/api/packages/offline?locationId=&languageCode=` | Tải ZIP (`content.txt` + MP3) |
| **Phân tích** | UC-05 | POST | `/api/analytics/logs` | Ghi log lượt nghe |
| | UC-06 | GET | `/api/analytics/dashboard` | Số liệu tổng quan cho CMS |
| **Tour** | UC-07 | GET | `/api/tours` | Danh sách tour kèm lộ trình |
| | — | GET | `/api/v1/tours` | Bản phân trang |
| | UC-07 | GET | `/api/tours/{id}` | Chi tiết 1 tour |
| **Ngôn ngữ** | — | GET | `/api/languages`, `/api/v1/languages` | Danh sách ngôn ngữ |
| | — | GET | `/api/languages/{id}`, `/api/v1/languages/{id}` | Chi tiết 1 ngôn ngữ |
| | — | POST | `/api/languages`, `/api/v1/languages` | Tạo ngôn ngữ (201 / 409) |
| | — | PUT | `/api/languages/{id}`, `/api/v1/languages/{id}` | Cập nhật ngôn ngữ |
| | — | DELETE | `/api/languages/{id}`, `/api/v1/languages/{id}` | Xóa ngôn ngữ (204) |

### 7.2. Hợp đồng lỗi thống nhất

Lỗi do `GlobalExceptionHandler` xử lý trả về cùng một cấu trúc 5 trường:

```json
{
  "timestamp": "2026-10-20T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Không tìm thấy ngôn ngữ với ID: 99",
  "path": "/api/languages/99"
}
```

| HTTP | `error` | Nguyên nhân | Áp dụng cho |
| :--- | :--- | :--- | :--- |
| 400 | `Bad Request / Not Found` | Lỗi nghiệp vụ chung (`RuntimeException`) | `locations`, `tours`, `packages`, `analytics` (POI không tồn tại) |
| 400 | `Bad Request / Not Found` | Lỗi serialize entity: `Could not write JSON: Cannot lazily initialize collection of role 'vn.edu.sgu.audiotour.entity.Location.subLocations' ... (no session)` | `GET /api/locations/by-qr/{qrCode}`, `GET /api/locations/{id}/sub-locations` — xem B-13 |
| 400 | `Validation Failed` | Sai định dạng/thiếu trường bắt buộc; message liệt kê `field: message` | `login`, `languages`, `analytics/logs` |
| 401 | `Unauthorized` | Sai tài khoản, token thiếu/sai/hết hạn | `login` (body 5 trường) |
| 401 | `Unauthorized` | Token thiếu/sai/hết hạn ở filter chain | Mọi endpoint `/api/**` — **body theo format lỗi mặc định của Spring Boot** (`timestamp/status/error/path`, không có `message`) |
| 404 | `Not Found` | `NotFoundException` | Chỉ `languages` (`GET/PUT/DELETE /api/languages/{id}`) |
| 409 | `Conflict` | Trùng `code` ngôn ngữ, xóa ngôn ngữ đang tham chiếu, vi phạm ràng buộc DB | `languages` |
| 500 | `Internal Server Error` | Lỗi hệ thống | Mọi nhóm |

> Lưu ý bất nhất: `LocationService`, `TourService`, `AudioPackageService` và `AnalyticsService` ném `RuntimeException` thay vì `NotFoundException`, nên "không tìm thấy" ở các nhóm này trả **400** chứ không phải 404. Xem backlog B-11.

---

## 8. Luồng nghiệp vụ chính

### 8.1. Luồng A — Quét QR (UC-03 → UC-04 → UC-05)

```
Du khách        Mobile App              Backend
    │  quét mã QR     │                    │
    │────────────────►│ GET /api/locations/by-qr/{qr} │
    │                 │───────────────────►│
    │                 │◄───────────────────│ 200 Location (kèm subLocations)
    │◄────────────────│                    │
    │  chọn ngôn ngữ  │                    │
    │────────────────►│ GET /api/packages/offline?locationId&languageCode
    │                 │───────────────────►│  đọc narrations + MP3 trên đĩa
    │                 │◄───────────────────│ 200 application/zip (tour-offline.zip)
    │  nghe offline   │                    │
    │────────────────►│ POST /api/analytics/logs  (triggerType=QR_CODE)
    │                 │───────────────────►│  ghi playback_logs
```

> Mã QR không tồn tại → 400; POI chưa có bản thuyết minh cho ngôn ngữ đã chọn → 400 (FR-PKG-03).
> ⚠️ Bước đầu tiên (`GET /api/locations/by-qr/{qrCode}`) hiện trả 400 do lỗi lazy-init — xem B-13. Luồng A chưa chạy trọn vẹn.

### 8.2. Luồng B — Geofence (UC-01 → UC-02 → UC-05)

```
Du khách        Mobile App              Backend
    │  đến gần POI   │                    │
    │────────────────►│ GET /api/locations/pois (1 lần, lưu SQLite)
    │                 │───────────────────►│
    │                 │◄───────────────────│ 200 danh sách POI + tọa độ + bán kính + priority
    │  App tự so sánh khoảng cách, vào vùng → tự phát audio
    │  gửi log sau khi nghe xong
    │────────────────►│ POST /api/analytics/logs  (triggerType=GEOFENCE)
    │                 │───────────────────►│
```

> Quyết định "vào vùng" nằm ở client. Backend cung cấp đủ `latitude`, `longitude`, `radiusInMeters`, `priority` để App tự đánh giá khi offline.

### 8.3. Luồng C — Quản trị danh mục ngôn ngữ

```
CMS                     Backend
  │ POST /api/languages  │  kiểm tra code trùng → tạo → 201
  │ PUT /api/languages/{id} │  kiểm tra code trùng (khác chính nó) → cập nhật
  │ DELETE /api/languages/{id} │  còn narration tham chiếu → 409
  │                        │  ngược lại → 204
```

---

## 9. Tiêu chí chấp nhận (Acceptance Criteria)

| # | UC | Tiêu chí | Cách kiểm chứng |
| :--- | :--- | :--- | :--- |
| AC-01 | — | `POST /api/auth/login` với tài khoản đúng trả 200 kèm token; sai trả 401 | `AuthIntegrationTest`, `AuthServiceTest` |
| AC-02 | — | Gọi endpoint `/api/**` không kèm token trả 401 | `AuthIntegrationTest` |
| AC-03 | UC-01 | `GET /api/locations` trả đúng cây địa điểm gốc, mỗi node kèm `subLocations` | `LocationControllerTest`, `SeedDataIntegrityTest` |
| AC-04 | UC-03 | Sáu mã QR của dữ liệu seed đều tra cứu được đúng địa điểm | ⛔ **Chưa đạt**: endpoint trả 400 do lỗi lazy-init (B-13). `SeedDataIntegrityTest` mới chỉ assert sự tồn tại của mã QR, chưa có test cho luồng tra cứu (B-12) |
| AC-05 | UC-04 | `GET /api/packages/offline` trả ZIP hợp lệ chứa `content.txt` + MP3 | `AudioPackageServiceTest`, `AudioPackageControllerTest` |
| AC-06 | UC-05 | `POST /api/analytics/logs` thiếu `locationId` hoặc `listenDurationSeconds` âm trả 400 kèm chi tiết field | `AnalyticsControllerValidationTest` |
| AC-07 | UC-06 | `GET /api/analytics/dashboard` trả đủ 5 nhóm số liệu | `AnalyticsServiceTest` |
| AC-08 | — | `POST /api/languages` với `code` trùng trả 409 | `LanguageServiceTest`, `LanguageControllerTest` |
| AC-09 | — | `DELETE /api/languages/{id}` khi còn narration tham chiếu trả 409 | `LanguageServiceTest`, `LanguageIntegrationTest` |
| AC-10 | — | `GET /api/v1/locations` trả đúng cấu trúc phân trang; `/api/locations` vẫn trả mảng phẳng | `PaginationIntegrationTest` |
| AC-11 | — | Lỗi trả đúng 5 trường `timestamp/status/error/message/path` | `GlobalExceptionHandlerTest` |
| AC-12 | — | Dữ liệu seed nhất quán: mọi narration trỏ tới location + language tồn tại, mọi file MP3 có trên đĩa | `SeedDataIntegrityTest` |
| AC-13 | — | Profile `prod` khởi động được với tài khoản admin cấu hình từ biến môi trường và đăng nhập thành công | `ProdProfileTest` |
| AC-14 | — | Toàn bộ test pass trên CI khi push commit | `.github/workflows/ci.yml` |
| AC-15 | — | Actuator: `/actuator/health` public, `/actuator/metrics` trả 401 khi thiếu token | `AuthIntegrationTest` |
| AC-16 | UC-08 | `GET /api/locations/{id}/sub-locations` trả 200 kèm danh sách phòng con | ⛔ **Chưa đạt**: endpoint trả 400 do lỗi lazy-init (B-13). `LocationControllerTest`/`LocationServiceTest` chỉ test với mock, không chạy với Hibernate thật |
| AC-17 | — | Các endpoint đã kiểm chứng runtime với MySQL thật trả đúng mã trạng thái | Kiểm chứng thủ công 20/10/2026: `/api/locations`, `/api/locations/pois`, `/api/locations/{id}`, `/api/tours`, `/api/tours/{id}`, `/api/languages`, `/api/analytics/dashboard` → 200; `/api/locations/by-qr/{qr}`, `/api/locations/{id}/sub-locations` → 400 (B-13) |

---

## 10. Chỉ số đo lường (KPIs)

| Chỉ số | Nguồn dữ liệu | Mục tiêu giai đoạn đầu | Thực trạng seed |
| :--- | :--- | :--- | :--- |
| Tổng lượt nghe | `playback_logs` | ≥ 500 lượt / tháng | 7 lượt mẫu |
| Tỷ lệ kích hoạt tự động (QR + GEOFENCE) | `listensByTriggerType` | ≥ 80% | 7/7 lượt mẫu là QR/GEOFENCE |
| Thời gian nghe trung bình / POI | `averageListenDurationSeconds` | 60–120 giây (không bỏ dở) | 92,9 giây |
| Tỷ lệ kích hoạt theo ngôn ngữ | `playback_logs.language_code` | Cân bằng `vi` / `en` | 5 `vi` / 2 `en` |
| Độ phủ thuyết minh đa ngôn ngữ | `narrations` vs `locations` | 100% POI bàn giao có ≥ 1 ngôn ngữ (mục tiêu mở rộng: ≥ 2 ngôn ngữ) | 6/6 POI có `vi`; 1/6 có `en` |
| Top POI tập trung | `topLocations` | Không POI nào > 40% lượt nghe (rủi ro phụ thuộc 1 điểm) | Tối đa 2/7 lượt (≈ 29%) — 3 POI cùng dẫn đầu |

---

## 11. Kiến trúc & công nghệ

| Hạng mục | Lựa chọn |
| :--- | :--- |
| Ngôn ngữ / Runtime | Java 21 (LTS) |
| Framework | Spring Boot 4.1.1 (WebMVC, Data JPA, Security, Validation, Actuator) |
| CSDL | MySQL 8.0+ (InnoDB, utf8mb4) |
| ORM | Spring Data JPA / Hibernate 7, `ddl-auto: validate`, `open-in-view: false` |
| Migration | Flyway (`V1__initial_schema.sql`) |
| Bảo mật | Spring Security + JWT (jjwt 0.12.6, HS256) |
| Tài liệu API | Springdoc OpenAPI 3 (springdoc-openapi 2.8.5) / Swagger UI |
| Tiện ích | Lombok (`@RequiredArgsConstructor` — constructor injection) |
| Kiểm thử | JUnit 5, Mockito, MockMvc, Spring Boot Test; CI dùng **MySQL 8 service container** của GitHub Actions (không dùng Testcontainers) |
| Báo cáo độ phủ | JaCoCo (`jacoco-maven-plugin`, report gắn vào phase `test`) |
| CI/CD | GitHub Actions (build → test → package JAR → artifact → build & push Docker image) |
| Đóng gói | Docker, `docker compose up --build` cho app + MySQL |

### 11.1. Phân tầng (3-Tier)

| Tầng | Thành phần | Trách nhiệm |
| :--- | :--- | :--- |
| **Presentation** | `AuthController`, `LocationController`, `AudioPackageController`, `AnalyticsController`, `TourController`, `LanguageController` | HTTP, validate input, đóng gói response, không chứa business logic |
| **Business / Application** | `AuthService`, `LocationService`, `AudioPackageService`, `AnalyticsService`, `TourService`, `LanguageService` | Nghiệp vụ, transaction, dựng file ZIP |
| **Data Access** | `LocationRepository`, `LanguageRepository`, `NarrationRepository`, `TourRepository`, `PlaybackLogRepository` | Truy vấn DB, query tổng hợp phân tích |

Luồng phụ thuộc đi theo một chiều: Controller → Service → Repository. Entity không gọi ngược lại Service.

---

## 12. Khoảng trống so với PRD lý tưởng (Backlog tương lai)

Các mục dưới đây **chưa có trong hệ thống hiện tại**, ghi lại để không ai hiểu nhầm là đã bàn giao:

| # | Hạng mục | Mức độ | Ghi chú |
| :--- | :--- | :--- | :--- |
| B-01 | API upload/nghiệp vụ CRUD cho POI, Narration, Tour | Cao | Hiện chỉ có CRUD cho `Language`; POI/Tour/Narration chỉ đọc và seed tay trong `DataInitializer` |
| B-02 | Quản lý nhiều tài khoản + phân quyền | Trung bình | Hiện 1 tài khoản admin đơn lẻ từ biến môi trường, không có DB user, không có refresh token |
| B-03 | Dịch máy tự động nội dung thuyết minh sang ngôn ngữ khác | Trung bình | TTS sinh audio đã có (`POST /api/narrations/{id}/audio`, ADR 0004); phần dịch văn bản chưa làm; mới 1/6 POI có bản `en`. Đặc tả: `docs/specs/feature-them-ngon-ngu.md`, quyết định: ADR 0005 |
| B-04 | Lưu trữ file âm thanh ngoài ổ đĩa cục bộ (S3/MinIO) | Trung bình | `uploads/audio` chỉ là thư mục cục bộ, không scale ngang |
| B-05 | Cache gói offline (Redis) | Thấp | Hiện nén ZIP lại từ đầu mỗi lần gọi |
| B-06 | Lọc dashboard theo khoảng thời gian / ngôn ngữ | Trung bình | Hiện thống kê toàn bộ lịch sử |
| B-07 | Rate limiting / chống lạm dụng endpoint log | Trung bình | Log endpoint chấp nhận mọi request hợp lệ từ token hợp lệ |
| B-08 | Xác thực geofence phía server | Thấp | Hiện hoàn toàn tin tín hiệu client, có thể giả mạo log |
| B-09 | Push notification khi đến gần POI | Thấp | Cần nền tảng FCM/APNs |
| B-10 | Phân trang cho `/api/analytics/dashboard` | Thấp | Heatmap trả toàn bộ điểm |
| B-11 | Chuẩn hoá mã lỗi "không tìm thấy" về 404 | Trung bình | `Location`/`Tour`/`Package`/`Analytics` đang trả 400; chỉ `Language` dùng `NotFoundException` → 404 |
| B-12 | Bổ sung test cho tra cứu QR (`GET /api/locations/by-qr/{qrCode}`) | Thấp | Đặc tả UC-03 chưa có test tương ứng |
| B-13 | Sửa lỗi lazy-init khi serialize `Location.subLocations` | **Cao** | `LocationRepository.findByQrCode` và `findByParentLocationId` **không có `@EntityGraph`**, trong khi `open-in-view: false` → entity trả về đã detach, Jackson gọi `getSubLocations()` lúc không còn session → `400 Could not write JSON: Cannot lazily initialize collection`. Ảnh hưởng trực tiếp **UC-03** và **UC-08**. Cách sửa: thêm `@EntityGraph(attributePaths = {"subLocations"})` cho 2 method đó (giống `findById`/`findAll` đang làm) |

---

## 13. Rủi ro

| Rủi ro | Mức độ | Giảm thiểu hiện có | Hướng xử lý |
| :--- | :--- | :--- | :--- |
| Rủi ro rò rỉ dữ liệu do CORS / endpoint mới không kèm guard | Cao | `denyAll` cho request không xác định; `/api/**` bắt buộc token | Rà soát `SecurityConfig` mỗi khi thêm controller |
| Dùng nhầm secret dev khi deploy | Cao | Profile `prod` không có default; compose từ chối start thiếu biến | Quy trình deploy bắt buộc khai báo `SPRING_PROFILES_ACTIVE=prod` |
| File MP3 lệch với `narrations.audioUrl` | Trung bình | `AudioPackageService` kiểm tra file tồn tại, trả lỗi rõ ràng | B-04 (lưu trữ ngoài) + kiểm tra định kỳ |
| ZIP nén lại mỗi request → tốn CPU khi nhiều người tải | Trung bình | — | B-05 cache gói theo `(locationId, languageCode)` |
| Sai lệch schema DB với entity | Thấp | Flyway + `ddl-auto: validate` | Migration mới phải có file `V2__...sql` |
| `DataInitializer` chạy lại trên DB cũ | Thấp | Chỉ seed khi `languages` rỗng | Không xóa bảng dữ liệu thật |
| Phân trang fetch-join bị Hibernate cảnh báo in-memory | Thấp | `findAllPaged` dùng `@Query` + `@EntityGraph`, đã ghi chú `shortcut` trong repository | Nâng cấp bằng cách phân trang theo id rồi fetch theo id khi dữ liệu lớn |
| Collection `LAZY` bị serialize sau khi session đã đóng | **Cao** | `findAll`, `findById`, `findByParentLocationIsNull` đã có `@EntityGraph` | B-13 — bổ sung `@EntityGraph` cho `findByQrCode` và `findByParentLocationId`; thêm integration test đọc entity qua HTTP để chặn tái phát |

---

## 14. Phụ lục

### 14.1. Biến môi trường

| Biến | Mặc định (dev) | Bắt buộc ở prod | Ghi chú |
| :--- | :--- | :---: | :--- |
| `SPRING_PROFILES_ACTIVE` | (không) | ✔ | `prod` |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `123456` | ✔ | |
| `DB_ROOT_PASSWORD` / `DB_NAME` | `root` / `audiotour_db` | ✔ | Dùng cho `docker-compose.yml` |
| `JWT_SECRET` | secret dev | ✔ | ≥ 32 byte (HS256) |
| `JWT_EXPIRATION_MS` | `86400000` | — | 24 giờ |
| `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD` | `admin` / `admin123` | ✔ | Username ≥ 3 ký tự |

### 14.2. Dữ liệu mẫu chuẩn bị bàn giao

| Mã QR | Địa điểm | Quận | Bán kính | Ưu tiên | Ngôn ngữ có sẵn |
| :--- | :--- | :--- | :---: | :---: | :--- |
| `QR-KHANH-HOI-01` | Trạm dừng xe buýt Khánh Hội | 4 | 35m | 2 | vi |
| `QR-VINH-HOI-01` | Trạm dừng xe buýt Vĩnh Hội | 4 | 30m | 1 | vi |
| `QR-XOM-CHIEU-01` | Trạm dừng xe buýt Xóm Chiếu | 4 | 30m | 3 | vi |
| `QR-DINH-DOC-LAP-01` | Dinh Độc Lập | 1 | 100m | 5 | vi |
| `QR-PHONG-KHANH-TIET` | Phòng Khánh Tiết (con) | 1 | 15m | 2 | vi, en |
| `QR-PHONG-DAI-YEN` | Phòng Đại Yến (con) | 1 | 15m | 2 | vi |

Mỗi dòng tương ứng một file MP3 thật trong `uploads/audio/`.

### 14.3. Tuyến tour mẫu

| Tuyến | Thời lượng | Điểm dừng |
| :--- | :---: | :--- |
| Tour Tuyến Xe Buýt Lịch Sử Quận 4 | 60 phút | Khánh Hội, Vĩnh Hội, Xóm Chiếu |
| Tour Khám phá Dinh Độc Lập | 90 phút | Dinh Độc Lập, Phòng Khánh Tiết, Phòng Đại Yến |

### 14.4. Ma trận truy vết Use Case → Yêu cầu → Endpoint → Kiểm thử

| Mã UC | Tên ca sử dụng | FR liên quan | Endpoint API | Lớp Controller / Service / Repository | Kiểm thử |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **UC-01** | Đồng bộ danh sách POI Geofencing | FR-LOC-01, FR-LOC-02 | `GET /api/locations/pois` | `LocationController` / `LocationService` / `LocationRepository` | `LocationControllerTest`, `SeedDataIntegrityTest` |
| **UC-02** | Tự động phát thuyết minh GPS ngoài trời | FR-LOC-02, FR-ANA-01, FR-ANA-05 | Logic Mobile + `POST /api/analytics/logs` | `AnalyticsController` / `AnalyticsService` / `PlaybackLogRepository` | `AnalyticsServiceTest`, `AnalyticsControllerValidationTest` |
| **UC-03** | Quét mã QR nhận thông tin địa điểm | FR-LOC-03, FR-LOC-04 | `GET /api/locations/by-qr/{qrCode}` | `LocationController` / `LocationService` / `LocationRepository` | ⛔ Endpoint lỗi runtime (B-13); `SeedDataIntegrityTest` mới chỉ assert mã QR tồn tại, chưa có test riêng (B-12) |
| **UC-04** | Tải trọn gói âm thanh Offline dạng ZIP | FR-PKG-01…04 | `GET /api/packages/offline` | `AudioPackageController` / `AudioPackageService` / `NarrationRepository` | `AudioPackageServiceTest`, `AudioPackageControllerTest` |
| **UC-05** | Ghi nhận nhật ký nghe thuyết minh | FR-ANA-01, FR-ANA-02, FR-ANA-04 | `POST /api/analytics/logs` | `AnalyticsController` / `AnalyticsService` / `PlaybackLogRepository` | `AnalyticsServiceTest`, `AnalyticsControllerValidationTest` |
| **UC-06** | Xem báo cáo thống kê Dashboard & Heatmap | FR-ANA-03, FR-ANA-05 | `GET /api/analytics/dashboard` | `AnalyticsController` / `AnalyticsService` / `PlaybackLogRepository` | `AnalyticsServiceTest` |
| **UC-07** | Xem lộ trình tuyến Tour tham quan | FR-TOUR-01…03 | `GET /api/tours`, `GET /api/tours/{id}` | `TourController` / `TourService` / `TourRepository` | `TourServiceTest`, `PaginationIntegrationTest` |
| **UC-08** | Tra cứu khu vực con bên trong di tích | FR-LOC-05, FR-LOC-06 | `GET /api/locations/{id}/sub-locations` | `LocationController` / `LocationService` / `LocationRepository` | ⛔ Endpoint lỗi runtime (B-13); `LocationControllerTest`, `LocationServiceTest` chỉ chạy với mock, không phát hiện lỗi lazy-init |

---

**Tài liệu liên quan:** `README.md` (hướng dẫn chạy), `docs/USE_CASES.md` (đặc tả ca sử dụng + biểu đồ), `docs/PID.md` (khởi tạo dự án), `docs/PROJECT_STRUCTURE.md` (cấu trúc), `docs/ROLLBACK.md` (quy trình rollback), `docs/COMPLETED_REPORT.md` (báo cáo bàn giao).
