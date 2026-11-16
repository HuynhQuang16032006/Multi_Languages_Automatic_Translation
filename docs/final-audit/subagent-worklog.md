# Nhật ký subagent

| ID | Vai trò | Phạm vi / kết quả | File sửa | Kiểm chứng |
|---|---|---|---|---|
| AUD-1 | BackendAudit, scout | Kiểm tra controller/service/repository, auth, Flyway; phát hiện thiếu unique DB cho cặp địa điểm/ngôn ngữ. Nhận định phải đối chiếu: không coi HTML dashboard public là bypass khi API vẫn yêu cầu JWT. | Không | Không chạy test; một số tài liệu chưa đọc; báo cáo source không chứng minh runtime |
| AUD-2 | FrontendAudit, scout | Audit tourist/admin, QR, audio và analytics; phát hiện QR mất trigger. Nhận định thiếu UI ZIP bị bác bỏ: `frontend/src/player.jsx` có liên kết tải. | Không | Không chạy test; không đọc hết mọi phần render |
| AUD-3 | DevopsAudit, scout | Kiểm tra Compose, Dockerfile, CI, cách ly test. Root DB và dev profile là giới hạn vận hành. Secret local bị gitignore không tự chứng minh rò rỉ; không in hoặc thay giá trị. | Không | Không chạy test hoặc Docker |
| FIX-1 | FixQr | Truyền `QR_CODE` từ scanner qua chi tiết, player, heartbeat; giữ `MANUAL` cho đường thường. | `frontend/src/qr.jsx`, `main.jsx`, `player.jsx`, `listening.js`, `listening.test.js` | Orchestrator chạy `npm test`: 51/51; `npm run build`: PASS. Chưa xác minh browser/E2E sau sửa |
| FIX-2 | FixReadme | Sửa mô tả React, route, phân quyền và số test lỗi thời. | `README.md` | Orchestrator đối chiếu `SecurityConfig` và route React; không có test tài liệu riêng |

Các nhiệm vụ audit chỉ đọc; hai nhiệm vụ sửa không chồng file. Agent không chạy build/test giữa chừng; orchestrator chạy sau tích hợp. Không commit, push, thay secret hay database. Migration unique chưa triển khai: cần môi trường DB test và kiểm tra dữ liệu trùng trước khi áp dụng vào DB hiện hữu.
