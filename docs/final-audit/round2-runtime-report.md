# Runtime và nghiệm thu vòng 2 — 2026-11-16

## Môi trường và an toàn

Windows, Java 21.0.12.1, Node 24.20.0, npm 11.19.0, MySQL 8.0.45. Docker daemon 29.9.3 hoạt động; Compose hiện hữu có DB healthy và app `8080` health UP. Không ghi vào DB Compose, không dừng container, không xóa volume.

JAR mới chạy trên `127.0.0.1:18081` với datasource `audiotour_test` ở 3306, tài khoản `audiotour_test` chỉ có quyền hai schema test; `GET /actuator/health` HTTP 200 UP. Log startup xác nhận schema test version 5 và validate 5 migrations. **Tác động ngoài dự kiến:** profile prod được thừa hưởng từ environment và `AuthService` cập nhật admin trong DB test khi khởi động. Không sửa credential cấu hình hoặc DB ứng dụng. Test và E2E cũng tạo dữ liệu trên DB test; fixture POI ID 238/narration, playback IDs 25/26 và log ZIP có thể còn lại. Không xóa dữ liệu ngoài fixture đã được xác định.

## Sửa lỗi và test

- G12: `V5__narration_location_language_unique.sql` thêm UNIQUE `(location_id, language_id)`. Trước migration, hai cột trong DB test NOT NULL và không có bản ghi trùng. Sau migration `flyway_schema_history` V1–V5 success, `information_schema.statistics` xác nhận index unique hai cột. Test cạnh tranh hai insert và NULL chạy MySQL; HTTP duplicate 409 có test hiện hữu. **Trước áp dụng DB thật:** kiểm tra nullable, duplicate, backup; không tự xóa duplicate; xem `docs/ROLLBACK.md`.
- G13: giới hạn `deviceId` 64 ở heartbeat, playback, offline; test HTTP 65, 64 và null. Không đổi schema.
- `mvnw.cmd test package` với `AUDIOTOUR_TEST_DB_PORT=3306` và npm cache riêng: **184 test, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS**, Vite build thành công. `npm test`: **51/51**, 0 fail/skip. `npm audit --omit=dev`: 0 vulnerabilities. Vite cảnh báo directive `use client` bị bỏ qua.
- `graphify update .` thành công; cảnh báo thiếu `tree_sitter_sql`, SQL không được trích vào graph.

## Runtime thật

- Admin: browser trên 18081, route quản trị yêu cầu login; sai password HTTP 401; đăng nhập thành công, tạo POI fixture ID 238 và narration vi, logout trở lại login. Chưa tạo audio cho fixture bằng TTS.
- Tourist/QR: nhập QR seed `QR-KHANH-HOI-01`, mở địa điểm 1; audio seed stream 206, browser decode duration 18.432s, tiến độ đạt 3.100s. Heartbeat trả 200. SQL test ghi playback ID 25 `QR_CODE`, ID 26 `MANUAL`; dashboard hiển thị cả hai, tổng 9. Chưa kiểm tra trực tiếp từng payload JOIN/PING/LEAVE, refresh, hay chính sách không đếm trùng.
- ZIP: browser nhấn tải từ player, nhận `tour-offline.zip` 144825 byte; Python `zipfile.testzip()` OK và có `content.txt`, `tram-khanh-hoi-vi.mp3`. API trả 200 `application/zip`; text khớp narration và audio khớp từng byte file thật SHA-256 `0d2c3c23fcd1ef6ac94e5c083690392cbb2a4cb7cce95a70bdfd663fb9b188b8`. Thiếu POI/ngôn ngữ trả 404. Chưa kiểm tra metadata trỏ file audio thiếu qua UI.
- Responsive: `/next/explore` 375×667, scrollWidth 375; trang chủ 375×812 không tràn. Console có ảnh Unsplash bị ORB, một request audio bị abort trước khi stream thành công. Tìm kiếm nhập nhanh qua automation mất ký tự; chưa xác định nguyên nhân, tìm qua URL `?q=E2E` cho đúng một kết quả.
- Camera/GPS thực địa, HTTPS trên điện thoại, từ chối quyền, mọi viewport và screen reader: chưa kiểm tra. PRD tự phát GPS khác kế hoạch chấp nhận gợi ý: NEEDS_TEACHER_CONFIRMATION.

## Dịch vụ bên ngoài

Test nội bộ trong Maven suite dùng mock upstream; không gọi ElevenLabs/DeepL thật và không xác nhận credentials hoặc chi phí. `BLOCKED_EXTERNAL` cho TTS/DeepL thật. `HttpClientConfig` không cấu hình connect/read timeout rõ ràng; thời hạn mặc định cụ thể chưa xác minh, cần kiểm tra và đặt timeout phù hợp. Onboarding có thể hoàn thành với một phần audio, trả số `audioGenerated` theo ADR 0005.

## Bằng chứng và giới hạn

Kết quả subagent: [round2-subagent-worklog.md](round2-subagent-worklog.md). Ma trận: [round2-acceptance-matrix.md](round2-acceptance-matrix.md). Không khẳng định browser E2E toàn bộ hoặc production-ready. API ZIP pass không chứng minh ZIP offline player. Không kiểm tra database ứng dụng thật, không gọi upstream trả phí, không commit/push/deploy.
