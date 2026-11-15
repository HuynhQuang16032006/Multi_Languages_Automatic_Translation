# Báo cáo nghiệm thu cuối — 2026-11-15

## Kết luận: NOT_READY

**Cập nhật vòng 2, 2026-11-16:** kết luận vẫn NOT_READY; backend test đã chạy, Maven 184/184 và một số luồng browser đã kiểm chứng. Bằng chứng, tác động tới DB test và blocker hiện hành tại [round2-runtime-report.md](round2-runtime-report.md) và [round2-acceptance-matrix.md](round2-acceptance-matrix.md). Phần dưới giữ làm lịch sử vòng 1.

Chưa đủ bằng chứng cho luồng bắt buộc trên bản hiện hành. MySQL test hiện kết nối tại cổng 3306; Docker daemon không kết nối và backend localhost:8080 chưa chạy. Không khẳng định môi trường sạch hoặc E2E đạt. Gap chi tiết tại [gap-analysis.md](gap-analysis.md), nhật ký tại [subagent-worklog.md](subagent-worklog.md), thao tác demo tại [demo-checklist.md](demo-checklist.md).

## Bảng nghiệm thu

| Hạng mục | Kết quả | Bằng chứng | Ghi chú |
|---|---|---|---|
| Requirements | PARTIAL | `docs/specs/PRD.md`, `docs/plans/academic-acceptance-plan.md` | PRD tự phát GPS khác kế hoạch chấp nhận gợi ý; cần xác nhận |
| Backend | PARTIAL | Full Maven suite 181 test đạt trên MySQL test | Chưa kiểm tra smoke API trên server chạy thật |
| Frontend Tourist | PARTIAL | `npm test` 51/51, Vite build đạt | Browser hiện hành sau sửa chưa kiểm tra |
| Frontend Admin | UNVERIFIED | Source và báo cáo local trước đó | Chưa demo bản hiện hành |
| Database | PARTIAL | MySQL 3306; tài khoản test chỉ có quyền trên hai schema test; Flyway V1–V4 thành công trên `audiotour_test` | Chưa khởi tạo môi trường sạch; schema chưa có unique cặp narration |
| Authentication/Security | UNVERIFIED | `SecurityConfig.java`, báo cáo local trước đó | Không chứng minh toàn bộ rủi ro hay security runtime hiện hành |
| API Integration | BLOCKED | localhost:8080 không kết nối | Không có API runtime để kiểm tra |
| Audio & Multilingual | PARTIAL | Unit audio 16/16; `docs/reports/audio-fix-verification.md` có playback lịch sử | TTS/DeepL thật và audio runtime hiện tại chưa kiểm chứng |
| QR Code | PARTIAL | Test payload `QR_CODE`; sửa truyền state | Chưa kiểm tra camera, browser hoặc dashboard end-to-end sau sửa |
| Analytics | PARTIAL | Báo cáo lịch sử có playback/dashboard | Chưa kiểm tra thống kê gắn với bản sửa QR |
| Unit Tests | PASS | Frontend 51/51 và Maven 181/181, 0 fail/skip | Số Maven bao gồm integration test |
| Integration Tests | PASS | `mvnw.cmd test`: 181 test, 0 failures/errors/skipped trên MySQL test cách ly | Không thay thế E2E browser |
| E2E | BLOCKED | Backend không chạy | Không suy diễn từ test payload |
| Docker/Build | PARTIAL | `docker compose config --quiet` thành công; package với cache riêng BUILD SUCCESS, frontend Vite build đạt | Docker daemon không sẵn sàng; chưa kiểm tra Docker build/start; Maven package bỏ qua test |
| Documentation | PARTIAL | README đã sửa; báo cáo và checklist trong `docs/final-audit/` | Chưa xác nhận mọi tài liệu cũ đồng bộ |
| Demo Readiness | BLOCKED | Backend chưa chạy; Docker daemon chưa kết nối | Cần demo đầy đủ và ghi kết quả |

## Thay đổi

- React giữ nguồn `QR_CODE` từ trang quét đến phiên nghe; heartbeat mang loại kích hoạt tương ứng. Test unit kiểm tra JOIN/PING/LEAVE; chưa chứng minh E2E.
- README cập nhật giao diện React song song và quyền API theo source.
- Không sửa migration, dữ liệu, secret hay API contract. Không commit/push/deploy.

## Lệnh và môi trường

Windows, OpenJDK 21.0.12.1, Node 24.20.0, npm 11.19.0. `npm test && npm run build`: 51 test pass, 0 fail/skip; Vite build thành công, cảnh báo React Router `use client` bị bỏ qua. `mvnw.cmd -Dtest=NarrationServiceTest,TtsServiceTest test`: 16 pass, 0 fail/skip. `mvnw.cmd -DskipTests package`: BUILD SUCCESS sau khi đặt `npm_config_cache` vào `target/npm-cache`; lần đầu lỗi EPERM tại npm cache mặc định. Build cuối sau sửa QR thành công và bỏ qua test. Kết quả `docker compose config --quiet` chỉ kiểm tra cấu hình, không kiểm tra container.

Sau khi MySQL được mở, `AUDIOTOUR_TEST_DB_PORT=3306` và `mvnw.cmd test`: 181 test, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS. `SHOW GRANTS` xác nhận tài khoản `audiotour_test@localhost` chỉ có quyền trên hai schema test. Schema `audiotour_test` có Flyway V1–V4 thành công; truy vấn nhóm không thấy narration trùng tại thời điểm kiểm tra. Không kiểm tra dữ liệu ứng dụng.

Báo cáo `docs/reports/audio-fix-verification.md` ghi playback, ZIP và dashboard từng hoạt động trên môi trường local với fixture; fixture audio được nối bằng SQL, không chứng minh admin upload hoặc TTS thật. Kết quả lịch sử không chứng minh bản hiện hành sau sửa QR.

## Việc bắt buộc còn lại

1. Khởi động backend với database test cách ly; kiểm tra khởi tạo từ database sạch nếu khả thi, không xóa volume hoặc sửa DB ứng dụng.
2. Xử lý rủi ro unique `(location_id, language_id)`: kiểm tra dữ liệu trùng trên bản sao trước migration, thêm ràng buộc có kế hoạch xử lý an toàn và kiểm tra cạnh tranh trên MySQL test.
3. Smoke trên JAR/Compose; browser thực hiện login → tạo địa điểm → narration/audio → tourist → QR → nghe → dashboard, xác minh trigger QR và ZIP. Ghi pass/fail thực tế.
4. Xác nhận với giảng viên yêu cầu GPS tự phát hay gợi ý; kiểm tra GPS/camera/HTTPS trên thiết bị phù hợp.
5. Kiểm chứng TTS thật khi có quyền và chi phí được chấp thuận; DeepL nếu thuộc tiêu chí. Không dùng mock làm bằng chứng dịch vụ production.

Chưa có số lượng tổng yêu cầu bắt buộc thống nhất: không đưa tỷ lệ hoàn thành tùy tiện. Bảng trên là 16 hạng mục nghiệm thu, không phải tập đầy đủ các yêu cầu đơn lẻ.
