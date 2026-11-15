# Ma trận nghiệm thu vòng 2

Đơn vị đếm: 12 gate theo yêu cầu phiên này. PASS chỉ trong phạm vi bằng chứng; `BLOCKED_EXTERNAL`, `UNVERIFIED_DEVICE`, `NEEDS_TEACHER_CONFIRMATION` là lý do chi tiết, không phải PASS.

| Gate | Yêu cầu | Kết quả | Bằng chứng / module | Ưu tiên | Còn lại |
|---|---|---|---|---|---|
| 1 | Backend startup | PASS | JAR 18081 health 200 UP; log datasource `audiotour_test` | P0 | Chưa xác minh image Compose mới |
| 2 | Frontend production build | PASS | Maven Vite build thành công | P1 | — |
| 3 | Database migrations | PARTIAL | V1–V5 thành công trên DB test, index unique có hai cột; test cạnh tranh đạt | P1 | Preflight bản sao DB ứng dụng và backup trước triển khai; chưa thử DB sạch |
| 4 | Authentication | PASS | Browser login, sai mật khẩu 401, protected route, logout; test Maven | P0 | Không tuyên bố pentest đầy đủ |
| 5 | Admin CRUD | PARTIAL | Browser tạo POI ID 238 và narration vi; Maven integration | P1 | Chưa kiểm tra đủ sửa/xóa UI và validation browser |
| 6 | Multilingual narration | PARTIAL | Seed vi phát, có tùy chọn ngôn ngữ; API/test nội bộ | P1 | Chưa kiểm chứng mọi ngôn ngữ, TTS thật `BLOCKED_EXTERNAL` |
| 7 | Audio playback thật | PASS | Browser seed phát 3.100s, duration 18.432s, stream 206 | P0 | Chất lượng ngôn ngữ/audio chưa đánh giá |
| 8 | QR → playback → analytics | PARTIAL | Browser nhập QR, nghe; DB `QR_CODE` ID 25 và `MANUAL` ID 26; dashboard hiện phiên | P1 | Chưa xác nhận JOIN/PING/LEAVE payload, refresh, chống đếm trùng; camera `UNVERIFIED_DEVICE` |
| 9 | ZIP download | PASS | UI tải ZIP; giải nén integrity OK, API nội dung text/audio khớp nguồn thật; lỗi POI/ngôn ngữ 404 | P1 | File audio thiếu qua UI chưa kiểm tra |
| 10 | Unit/integration regression | PASS | Maven 184/184, frontend 51/51, 0 fail/skip | P0 | Không thay E2E |
| 11 | Demo checklist | PARTIAL | `demo-checklist.md`, một số luồng browser thực | P1 | Cần demo toàn tuyến, thiết bị thực và audio/TTS theo yêu cầu |
| 12 | Không còn P0/P1 | FAIL | Gate 3, 5, 6, 8, 11 còn PARTIAL; GPS mâu thuẫn | P1 | Đóng các bằng chứng thiếu và chốt GPS |

**Kết luận: NOT_READY.** 6 PASS, 5 PARTIAL, 1 FAIL, 0 BLOCKED/UNVERIFIED ở cấp gate; các phần `BLOCKED_EXTERNAL`/`UNVERIFIED_DEVICE` nằm trong gate PARTIAL. Không coi count gate là phần trăm yêu cầu nghiệp vụ.
