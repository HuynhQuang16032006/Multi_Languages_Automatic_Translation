# Nghiệm thu tích hợp local Audio Tour

## 1. Executive Summary

**Kết luận: FAIL trong phạm vi nghiệm thu nghiệp vụ local.** Database, CRUD, JWT, dashboard và persistence đạt các kiểm tra đã chạy; thiếu audio trong image runtime là lỗi **P1** chặn nghe và offline. Không sửa mã sản phẩm để che lỗi. Không phát hiện P0 trong phạm vi kiểm tra; không đồng nghĩa đã chứng minh hệ thống không có P0.

## 2. Phạm vi và môi trường

Windows, Java 21, Spring Boot 4.1.1, MySQL 8.0, Docker Desktop. Backend `http://localhost:8080`, React `/next`, legacy `/`. Chỉ kiểm tra local. Không chỉnh `.env`, schema, cấu hình hay mã sản phẩm; không thao tác xóa/reset volume. Tái sử dụng 173 Maven tests và compile đã đạt từ mốc local; không chạy lại suite.

## 3. Ma trận chức năng

| Hạng mục | Trạng thái | Bằng chứng / giới hạn |
|---|---|---|
| Volume, schema, Flyway, FK | Passed | Đúng volume mới; V1–V4 thành công, checksum khớp nguồn; 5 FK, 0 orphan |
| Admin login | Passed | POST `/api/auth/login` 200; sai tài khoản 401 |
| Bảo vệ admin | Passed | GET `/api/admin/locations`, `/api/analytics/dashboard` không JWT: 401 |
| CRUD POI | Passed | POST 201, PUT 200, DELETE POI tạm 204; POI có dữ liệu liên quan 409 |
| Validation POI | Passed | Tên rỗng, latitude 91: 400, thông báo field |
| Thuyết minh CRUD | Passed | POST 201, GET/PUT 200, DELETE bài tạm 204; trùng địa điểm/ngôn ngữ 409 |
| Ngôn ngữ CRUD | Passed | GET vi/en/zh/ja; mã test riêng: POST 201, PUT/GET 200, trùng 409, DELETE 204 |
| QR lookup/update | Passed | GET QR 200; PATCH đổi QR 200, QR cũ 404, QR mới 200 |
| Visibility | Passed | PATCH false 200; đọc public 404; PATCH true 200 trên POI test |
| Pagination | Passed | locations size 2: totalElements 6, totalPages 3; languages size 2: totalElements 4 |
| Stream audio seed | Failed | HTTP 400 wildcard Accept, file `/app/uploads/audio/tram-khanh-hoi-vi.mp3` không tồn tại; thư mục audio thiếu |
| Audio upload | Not Implemented | Không có multipart/upload endpoint trong controller/OpenAPI; có TTS endpoint |
| TTS/dịch máy thật | Blocked | Không có key; TTS narration test trả 503 đúng hợp đồng; không gọi dịch vụ ngoài |
| Offline ZIP | Blocked | Thiếu audio runtime; POI test chưa có audio trả 400; chưa kiểm tra archive/range thành công |
| Playback log và dashboard | Passed | POST log 200; tổng lượt 7 thành 8; SQL lưu log 8 duration 12, QR_CODE đúng fixture |
| Heartbeat | Passed | JOIN/PING: ACTIVE, activeUsers 1; LEAVE: LEFT, activeUsers 0 |
| Dashboard lọc ngày rỗng | Passed | Khoảng 2099 trả 200, tổng 0 |
| Dashboard ngày đảo ngược | Not Tested | Có quan sát from > to trả 200/rỗng; không chấm lỗi khi chưa có contract bắt buộc 400 |
| React Home/Discovery/detail | Passed | Browser tải dữ liệu thật, 6 POI; detail1 có nội dung và vi/en/zh/ja |
| Search empty state | Passed | Chuỗi không khớp hiển thị 0 địa điểm và thông báo thử tên khác |
| Search positive/filter/sort | Blocked | Công cụ fill/type làm mất ký tự input React; chưa có bằng chứng hợp lệ cho kết quả dương; không tính là lỗi sản phẩm |
| React QR form | Passed | `/next/qr` có nhập mã/liên kết, nút tìm và mở camera; chưa quét camera thật |
| React Admin login/dashboard | Passed | Browser nhập tài khoản local, login thành công, dashboard 7 lượt nghe, active0, bảng phiên và thống kê |
| Legacy visitor | Passed | `/` hiển thị bản đồ Leaflet/Esri và 6 điểm; không bấm phát để tránh tạo log trên dữ liệu seed |
| Responsive mẫu | Passed | Screenshot desktop1440 và Discovery mobile375; không thấy tràn ngang ở mẫu mobile; không đại diện mọi trang |
| Image fallback | Passed | Ảnh Xóm Chiếu từ Unsplash bị ORB; UI hiện Không có ảnh địa điểm |
| Loading/API error/fallback khác | Not Tested | Không ép lỗi mạng hoặc làm giả trạng thái ứng dụng; chỉ quan sát image fallback |
| Frontend build/runtime | Passed | Tái dùng Maven frontend build ở mốc local; asset React thực sự render trong Chrome, không build lại |
| Persistence sau restart | Passed | Snapshot fixture và Flyway trước/sau restart giống hệt; đọc API đúng dữ liệu |
| JWT/security/runtime | Passed | Các probe JWT không hợp lệ 401; bindings đúng; health200 UP, DB healthy; xem giới hạn scan bên dưới |

## 4. Bằng chứng thực tế

Fixture nhận diện bằng prefix `ACCEPT-20261114235557`: POI `7`, narration `25` (vi), playback `8`, device `acceptance-a820939e-45ff-4265-8195-5209894200bb`. QR đổi từ prefix gốc sang hậu tố `-QR`. POI tạm `8`, con `9`, narration `26`, language `5` chỉ phục vụ CRUD, đã xóa qua API. Không sửa POI seed.

SQL read-only xác nhận POI 7 và narration 25 có văn bản cập nhật; playback 8 có location_id 7, device đúng, listen_duration_seconds 12, trigger_type QR_CODE.

`GET /api/packages/stream?locationId=1&languageCode=vi`, Bearer + `Accept: */*`: HTTP 400 với thông báo file không tồn tại `/app/uploads/audio/tram-khanh-hoi-vi.mp3`. Container kiểm tra `/app/uploads/audio`: `AUDIO_DIRECTORY_MISSING`. `Accept: application/json` trả 500 `No acceptable representation`; anonymous `Accept: audio/mpeg` trả 401. Các response không nhất quán được ghi riêng với lỗi thiếu file.

### API và browser

- OpenAPI `/v3/api-docs` 200 đối chiếu controller: không có endpoint upload audio; có stream/offline và TTS. Backend POI search/filter theo tên không được triển khai trong controller; frontend có lọc client-side, không giả định query search server.
- Validation log duration âm 400; location không tồn tại 404; QR trùng 409; narration trùng ngôn ngữ 409; xóa POI có phụ thuộc 409. Các body validation đã quan sát không chứa SQL.
- Hierarchy: tạo POI con9 của7 trả201, GET sub-locations có đúng child, xóa child204.
- Browser Chrome có sẵn, không cài công cụ mới. Screenshot desktop React: `C:\Users\User\AppData\Local\Temp\omp-sshots-15ce0e3259d90bac.webp`; screenshot mobile và legacy đã xuất trực tiếp trong transcript. Artifact ảnh nằm ngoài repo, có thể hết hạn theo temp cleanup.
- React detail hiển thị text thật từ API; admin dashboard browser khớp tổng7 sau cleanup. Không sửa DOM/storage để tạo trạng thái giả. Tab kiểm thử của parent đã đóng.
- Công cụ browser làm mất ký tự khi fill/type search, ví dụ chuỗi yêu cầu `Khánh Hội` thành `Khánh ội`. Timeout tìm kiếm dương không được dùng kết luận Failed. Timeout tìm chuỗi `Tổng quan` khác casing cũng không phải lỗi login: dashboard thực tế đã render.

### JWT, secrets và runtime

- Anonymous location write, translation write, password change, admin dashboard, actuator info/metrics:401. Login đúng200; dashboard/info/metrics có JWT200. Login sai401, malformed login400.
- JWT HS256, TTL86400 giây. Token malformed/empty/tampered/appended signature/wrong secret/expired/thiếu credential claim/unsigned đều401. Không đổi mật khẩu tài khoản hiện có, chưa kiểm tra thu hồi JWT bằng password rotation.
- Headers: `nosniff`, frame `DENY`, `Cache-Control: no-store`. Profile runtime `prod`; cấu hình dev có fallback local, prod đọc secret bắt buộc. Không kiểm tra lại khởi động profile khác.
- Ba secret phân biệt, không rỗng trong `.env` được so khớp nội bộ với 286 file tracked và log app/DB:0 match. `.env` không tracked. Không quét lịch sử Git, không kết luận toàn bộ lịch sử không có secret. Response login chứa JWT theo thiết kế, không đưa JWT vào báo cáo.
- Snapshot trước restart: app461.9MiB/CPU0.27%/52PID; DB378MiB/CPU0.70%/48PID, không OOM. Không phải benchmark. Log DB không có error/exception tại mẫu kiểm tra; app có lỗi audio và lỗi kết nối1130 lịch sử.
- App restart count10 là lịch sử trước kiểm thử persistence, không phải 10 lần lỗi mới. Sau restart có kiểm soát app/DB restart count0; app start `2026-11-14T17:02:01.583887966Z`; health cuối200 UP, DB healthy.


## 5. Luồng End-to-End

Admin login, tạo POI, thêm/sửa narration, tra QR, ghi log và xem dashboard: đã có bằng chứng. Bước tạo/phát audio chưa thực hiện được trên triển khai hiện tại; log gửi trực tiếp API không chứng minh người dùng đã nghe. Không tuyên bố E2E trọn vẹn Passed.

## 6. Database và volume

| Volume | Vai trò | Bằng chứng |
|---|---|---|
| `multi_languages_automatic_translation_audiotour-db-data` | Cũ, được bảo vệ | Tồn tại; CreatedAt `2026-11-14T16:45:40Z`; không container nào tham chiếu, kể cả stopped; không mount/ghi/xóa |
| `audiotour-mysql-local-v5` | Hiện hành | CreatedAt `2026-11-14T16:47:57Z`; chỉ DB gắn RW tại `/var/lib/mysql`; trước/sau restart không đổi |

Schema `audiotour_db` không trống: 8 bảng; Flyway V1–V4 thành công, checksum nguồn lần lượt `-1297627225`, `-967431311`, `-646331220`, `1349583146`. Năm FK có0 orphan, delete/update NO ACTION. Một admin dùng BCrypt; `ddl-auto: validate` và Flyway giữ nguyên.

Restart có kiểm soát: trước restart dashboard0 activeUsers, processlist không có truy vấn nghiệp vụ chạy, INNODB_TRX rỗng. Chạy `docker compose stop app`, `docker compose restart db`, `docker compose up -d --no-build --wait --wait-timeout 90`. Không recreate/reset DB, không thay volume. Compose cảnh báo volume cũ không khớp tên cấu hình và ghi rõ `left untouched`; không làm theo gợi ý xóa volume.

Sau restart, SQL của POI7/narration25/playback8 và Flyway giống snapshot trước restart; API trả tên và QR cập nhật đúng. Cleanup log chỉ bằng transaction `DELETE` có đủ điều kiện `id=8 AND location_id=7 AND device_id=<device test>`: ROW_COUNT1; không có API xóa log nên cleanup SQL chỉ giới hạn fixture đã xác minh. Xóa narration25 rồi POI7 qua API đều204. Không đổi schema/reset AUTO_INCREMENT.

Cuối kiểm thử:6 locations,24 narrations,7 playback_logs,0 package_downloads,4 languages; prefix fixture còn0 POI. Dashboard trở lại7 lượt, active0. Không chuyển dữ liệu giữa volume; không chạy bất kỳ lệnh prune/down -v nào.

## 7. Lỗi P0 / P1 / P2

- **P1 — deployment/integration:** image runtime không có `/app/uploads/audio`, stream MP3 và offline không dùng được dù metadata seed tồn tại. Dockerfile chỉ chép JAR; Compose không mount audio.
- **P2 — error handling:** stream thiếu audio có thể trả 401/500/400 tùy Accept/auth, làm sai chẩn đoán client; response còn lộ đường dẫn nội bộ.
- **P2 — least privilege:** backend kết nối DB bằng root. Cần user chỉ có quyền phù hợp khi được duyệt hardening, không tự thay tài khoản trong đợt nghiệm thu.
- **P2 — tài nguyên ảnh bên ngoài:** ảnh Xóm Chiếu bị Chrome ORB; fallback hoạt động, không chặn CRUD hay trang chi tiết. Kiểm tra nguồn ảnh ổn định khi được duyệt.
- **P2 — tài liệu lệch trạng thái:** `docs/specs/feature-dashboard-admin-login.md` vẫn ghi Chưa triển khai, trong khi API/browser chứng minh login đã có; tài liệu nghiệm thu cũ có số test/ưu tiên khác. Báo cáo này dùng mức P0/P1 theo yêu cầu hiện tại, không hạ mức để PASS.

## 8. Giới hạn

Chưa kiểm tra GPS/camera QR trên điện thoại thật, tải dịch vụ TTS/DeepL thật, load/stress/production. Không thay đổi mật khẩu admin để tránh ảnh hưởng tài khoản hiện tại. Chưa có baseline hash lịch sử volume cũ nên không thể khẳng định nội dung byte-for-byte không đổi trước thời điểm audit.

Không xác minh thành công audio range/ZIP contents, browser nghe thật, TTS/DeepL thật, QR PNG download/scan thật, mọi route admin responsive, loading/API error injection, positive search/sort, full CORS cross-origin/Vite dev server. Deployment cùng origin đã kết nối API; `vite.config.js` dùng base `/next/`, proxy `/api` tới `127.0.0.1:8080`, không chạy Vite server riêng. Không kiểm thử rate-limit đến mức khóa login để tránh gián đoạn. Truy cập tài liệu OpenAPI và một số lỗi có tính chất read-only; không fuzzing hay quét diện rộng.

## 9. Ưu tiên khắc phục

1. Xin phê duyệt sửa đóng gói/lưu trữ audio: đưa asset seed thật vào runtime và thiết kế persistence audio phù hợp; không mount hoặc đổi volume MySQL.
2. Sau sửa, chạy lại có mục tiêu stream/range/ZIP/browser playback và E2E analytics.
3. Chuẩn hóa lỗi stream và không lộ đường dẫn nội bộ.
4. Sau phê duyệt, giảm quyền DB, kiểm tra lỗi ảnh và cập nhật trạng thái SPEC; không gộp vào sửa audio nếu không cần.

## 10. Kết luận nghiệm thu

**FAIL — chưa nghiệm thu nghiệp vụ local.** Lỗi P1 thiếu audio làm luồng cốt lõi người dùng nghe và tải offline không đạt. Database và volume được bảo vệ trong các thao tác đã chạy; core CRUD/JWT/analytics/persistence đạt kiểm tra có bằng chứng. Không đánh đồng log gửi trực tiếp với lượt nghe thật, không nâng Blocked/Not Tested thành Passed. Có thể nghiệm thu lại có mục tiêu sau khi người dùng duyệt khắc phục P1; không cần chạy lại173 test nếu không có thay đổi liên quan.
