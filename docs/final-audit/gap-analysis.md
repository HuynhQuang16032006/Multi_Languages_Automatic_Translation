# Gap analysis — 2026-11-15

Phân loại theo yêu cầu phiên nghiệm thu; PASS chỉ áp dụng phạm vi có bằng chứng. Báo cáo trước là kết quả lịch sử, không chứng minh môi trường hiện tại. P0/P1 dùng định nghĩa trong yêu cầu phiên này, không dùng mức khác của kế hoạch cũ.

| ID | Module | Yêu cầu | Hiện trạng | Mức | Bằng chứng | Hành động |
|---|---|---|---|---|---|---|
| G1 | Integration | Demo luồng bắt buộc trên hệ thống hiện tại | BLOCKED | P1 | MySQL test mở cổng 3306; Docker daemon và backend localhost:8080 chưa kết nối | Khởi động backend dùng DB test rồi kiểm tra luồng |
| G2 | TTS/DeepL | Sinh audio/dịch thật | UNVERIFIED | P1 | `docs/plans/academic-acceptance-plan.md` F14; báo cáo audio mục 9 chưa gọi upstream | Kiểm tra với tài khoản được phép và chấp thuận chi phí |
| G3 | React QR/analytics | Phân loại lượt nghe QR | PARTIAL | P1 | `qr.jsx`, `main.jsx`, `player.jsx` truyền nguồn; `npm test` 51/51 gồm payload QR; chưa có E2E sau sửa | Xác minh QR → phát → dashboard với database thật |
| G4 | React/offline | Tải ZIP từ giao diện mới | UNVERIFIED | P2 | `frontend/src/player.jsx:102` có liên kết ZIP; kết quả ZIP runtime lịch sử tại `docs/reports/audio-fix-verification.md` | Kiểm tra lại từ giao diện hiện tại khi có runtime |
| G5 | GPS | Tự phát theo PRD | NEEDS_CONFIRMATION | P1 | `docs/specs/PRD.md` FR-2.1 khác `docs/plans/academic-acceptance-plan.md` F3 chấp nhận gợi ý | Xác nhận tiêu chí với giảng viên; không tự đổi nghiệp vụ |
| G6 | README | Mô tả frontend và API đúng hiện trạng | PASS | P2 | README đã đối chiếu ADR 0007, `SecurityConfig.java:41-57` và route React | Các phần khác vẫn cần rà soát thực tế |
| G7 | DB | Migrations, constraint, database sạch | PARTIAL | P1 | MySQL test 3306, quyền giới hạn hai schema; Flyway V1–V4 thành công tại `audiotour_test` | Kiểm tra khởi tạo DB sạch và các constraint còn thiếu |
| G8 | Thiết bị | Camera QR, GPS, HTTPS, responsive | UNVERIFIED | P1 | `docs/reports/audio-fix-verification.md` mục 9 | Kiểm tra thiết bị thật |
| G9 | Bảo mật | Container production, least privilege | PARTIAL | P2 | `docker-compose.yml:37-40` dùng root; Dockerfile không ép profile khi chạy độc lập | Giữ cấu hình; cân nhắc user DB riêng sau khi duyệt |
| G10 | Build | Production artifact | PASS | P2 | `mvnw.cmd -DskipTests package` thành công với npm cache riêng; Vite build thành công | Chạy artifact khi có DB |
| G12 | Database | Một narration cho mỗi địa điểm/ngôn ngữ | PARTIAL | P1 | `V1__initial_schema.sql:29-38` không có UNIQUE `(location_id, language_id)`; F14 trong kế hoạch chốt duy nhất; kiểm tra ứng dụng không loại trừ race | Trước migration phải kiểm tra trùng trên bản sao DB; thêm constraint và kiểm thử cạnh tranh trên MySQL test |
| G13 | Analytics input | Giới hạn deviceId theo cột DB | PARTIAL | P2 | DTO thiếu giới hạn độ dài; cột `device_id` giới hạn 64 | Xác minh lỗi và thêm validation khi có test runtime |
| G11 | Test | Frontend và Maven suite | PASS | P1 | `npm test` 51/51; `mvnw.cmd test` 181/181 với MySQL test, 0 fail/skip | Tiếp tục smoke và E2E |

Giới hạn: chưa kiểm tra toàn bộ yêu cầu hoặc xác nhận mọi nhận định subagent; không suy ra hệ thống không có lỗi khác. API upload âm thanh chưa thấy; F14 yêu cầu TTS, không tự coi upload là bắt buộc.
