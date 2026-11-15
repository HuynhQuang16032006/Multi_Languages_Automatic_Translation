# Nghiệm thu vòng 4 — 2026-11-16

## Kết luận: NOT_READY

React QR mới đã dùng mã gốc và tra API; vẫn thiếu nghiệm thu UI xóa/confirmation, nhập nhanh mất ký tự trong browser automation, QR truy cập LAN chưa hoạt động trên instance bind loopback, TTS/DeepL thật và quyết định GPS chưa có bằng chứng. Không dùng frontend legacy để chấm React.

## P1 và kết quả

| Hạng mục | Trạng thái | Bằng chứng / còn lại |
|---|---|---|
| QR admin React | PARTIAL | `admin-qr.jsx` tạo `/next/qr?code=...`; browser preview đúng mã seed, tra `by-qr` rồi React detail/player. Mã không tồn tại 404; đổi mã bằng SQL **trên DB test tạm**, mã cũ 404/mã mới 200, đã khôi phục. Chưa xác minh đổi mã qua UI do confirmation automation; LAN URL preview connection refused vì server bind loopback. QR legacy `/player.html` được giữ. |
| QR heartbeat | PARTIAL | Browser ghi JOIN, PING, LEAVE `QR_CODE`; MP3 thật duration 18.432s. Refresh ghi JOIN/PING, SQL log 6 giây; chưa bắt LEAVE network lúc refresh hoặc chứng minh chống trùng trên mọi race. |
| Admin CRUD | PARTIAL | Route bảo vệ hiện login; browser login/logout; tạo POI 201, sửa 200; tạo narration 201, sửa 200. Confirmation xóa qua browser chưa thành, không thấy DELETE; chưa chứng minh xóa UI/DB consistency. |
| ZIP/audio | PASS trong phạm vi | Browser tải ZIP `tour-offline.zip` 144825 bytes; `content.txt` khớp narration, audio 147456 bytes khớp stream SHA-256 `0d2c3c23fcd1ef6ac94e5c083690392cbb2a4cb7cce95a70bdfd663fb9b188b8`; missing POI/ngôn ngữ 404, UI địa điểm thiếu thông báo. Không chứng minh file bị thiếu trên đĩa qua UI. |
| Tìm kiếm | PARTIAL | `matchesPlace` hỗ trợ dấu/không dấu và đ; URL `?q=Dinh%20doc%20lap` trả Dinh Độc Lập. Browser nhập nhanh `Dinh doc lap` thành `Din dca`; chưa xác định lỗi automation hay UI, cần tái kiểm tra trên thiết bị/người dùng thật và sửa nếu xác nhận. |
| MySQL/migration | PASS trên môi trường test | MySQL 8.0 container tạm, schema trống; Flyway V1–V5 success, test unique/concurrency trong full suite. Không chạy migration DB ứng dụng; cần backup và preflight duplicate/nullability trên bản sao trước triển khai. |
| Startup/admin | PASS có giới hạn | JAR 18081 health UP trên DB test; `AuthService` giữ admin sẵn có, test trong suite. Không thay credentials ứng dụng. Runtime kiểm thử dùng profile dev riêng, không chứng minh production deployment. |
| TTS/DeepL | BLOCKED_EXTERNAL | `HttpClientConfig` connect 5s/read 30s, test server localhost timeout; không có retry tự động. Test upstream mock không chứng minh nhà cung cấp thật. Cần được phép chi phí, xác minh key/voice, gọi dịch/TTS thật, kiểm tra MP3, metadata và lỗi quota/timeout. |
| GPS/camera | NEEDS_TEACHER_CONFIRMATION / UNVERIFIED_DEVICE | PRD yêu cầu geofence tự phát; kế hoạch nghiệm thu chấp nhận gợi ý. Chưa kiểm tra GPS/camera/HTTPS điện thoại thật. Không tự thay đổi nghiệp vụ. |

## Test, build, môi trường

- `npm test`: **53 pass, 0 fail, 0 skip** sau sửa QR và tìm không dấu.
- `mvnw.cmd package` với MySQL tạm 33316: **189 tests, 0 failures/errors/skipped; BUILD SUCCESS**; Vite production build thành công. Cảnh báo React Router `use client` bị bỏ qua.
- `GET /actuator/health` trên JAR mới: HTTP 200 UP. MySQL `flyway_schema_history`: V1–V5 success.
- Browser 375×812: scrollWidth 375; trang React discovery và player hiển thị. Ảnh ngoài có fallback; chưa kiểm tra toàn bộ viewport/a11y.
- Chưa build Compose image mới; không dừng/thay đổi Compose hiện hữu. JAR và DB test do phiên này tạo được dừng sau kiểm tra.

## Thay đổi và subagent

- `Round4Qr`: `frontend/src/admin-qr.jsx`, `qr.jsx`, `qr.js`, `qr.test.js`: link React, resolve qua backend, test URL và concurrency.
- `Round4Backend`: `src/main/java/vn/edu/sgu/audiotour/config/HttpClientConfig.java`, `src/test/java/vn/edu/sgu/audiotour/config/HttpClientConfigTest.java`: timeout và test localhost.
- `Round4Search` bị hủy vì không tiến triển; orchestrator thêm `frontend/src/search.js`, `search.test.js`, cập nhật `main.jsx` cho tìm không dấu. Nhập nhanh chưa giải quyết.
- `Round4Audit`: audit read-only; `Round4QrE2E`, `Round4ZipE2E` kiểm tra runtime. `Round4AdminE2E` bị hủy vì không tiến triển; orchestrator kiểm tra một phần CRUD. Báo cáo này do orchestrator tổng hợp.
- Legacy `src/main/resources/static/` không bị sửa hoặc xóa trong vòng 4. API/security/database/audio production không đổi. Không commit/push/deploy.

## Demo sau khi khắc phục

1. Khởi động môi trường riêng với DB đã backup/preflight; xác nhận health và `/` mở React `/next/explore`.
2. Đăng nhập `/next/admin`, tạo/sửa/xóa POI và narration fixture; kiểm tra confirmation, logout, token không còn truy cập API.
3. Tạo QR tại `/next/admin/qr`, dùng host HTTPS/LAN thực sự truy cập được; quét bằng điện thoại, xác nhận code lookup, phát audio, dashboard ghi QR_CODE; đổi mã và chứng minh mã cũ 404.
4. Tải ZIP từ React player, kiểm tra text/audio; thử lỗi thiếu, search có dấu/không dấu và responsive.
5. Trình bày geofence là gợi ý cho đến khi giảng viên xác nhận; không tuyên bố TTS/DeepL thật khi chưa gọi có phép.
