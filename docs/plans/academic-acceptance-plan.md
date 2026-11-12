# Kế hoạch nghiệm thu đồ án Audio Tour (mức đại học)

**Mục tiêu:** Điều chỉnh tiêu chí nghiệm thu từ mức production-grade sang mức phù hợp với đồ án môn Công nghệ phần mềm tại đại học, nhưng không làm giảm chất lượng các chức năng cốt lõi đã chốt trong PRD/SPEC.

Nguồn tham khảo: `docs/plans/audio-tour-open-items.md`, `docs/PRD.md`, `docs/specs/SPEC.md`, `docs/plans/audio-tour-rebuild.md`, `docs/COMPLETED_REPORT.md`.

---

## 1. Nguyên tắc phân loại

| Mức | Ý nghĩa | Chặn nghiệm thu? |
|-----|---------|------------------|
| **P0** | Bắt buộc: bảo mật cơ bản, tính đúng đắn nghiệp vụ, an toàn database, kiểm thử tích hợp luồng chính, khả năng demo thực tế. | **Có** — phải đạt hoặc Accepted Limitation rõ ràng. |
| **P1** | Nên hoàn thành: chất lượng tốt hơn, thiết bị thật, responsive, CI/CD, Docker. | Không tự động chặn, trừ rubric môn học yêu cầu. |
| **P2** | Nâng cao: multi-instance, concurrency chuyên sâu, rollback rehearsal trên database production thật, kiểm thử toàn diện mọi thiết bị, tương thích artifact cũ. | **Không** — không yêu cầu khắc phục P2 trước khi hoàn thành P0. |

---

## 2. Tiêu chí nghiệm thu đã điều chỉnh (mức đại học)

### 2.1 Chức năng cốt lõi (giữ nguyên theo PRD/SPEC, không xóa tính năng)

| # | Tiêu chí | Nguồn | Mức | Trạng thái mục tiêu |
|---|----------|-------|-----|---------------------|
| F1 | Du khách truy cập web, quét QR hoặc mở link, nghe thuyết minh mà không cần đăng ký/tải app. | PRD 1.2, FR-2 | P0 | Demo được |
| F2 | Chế độ nghe thủ công: bấm điểm ghim trên bản đồ, phát audio theo ngôn ngữ hiện hành. | PRD FR-2.2 | P0 | Demo được |
| F3 | Chế độ nghe tự động GPS Geofence (chấp nhận chỉ gợi ý, không tự phát theo thỏa thuận người dùng). | PRD FR-2.1 | P0 | CóUI, hiển thị gợi ý |
| F4 | Quét mã QR tại chỗ, gọi `GET /api/locations/by-qr/{qrCode}`, phát audio `triggerType = QR_CODE`. | PRD FR-2.3 | P0 | Có API |
| F5 | Chọn ngôn ngữ (`vi`, `en`, `zh`, `ja`), lưu vào `localStorage`, giữ khi reload. | PRD FR-3.1 | P0 | Hoạt động |
| F6 | Kho âm thanh: 24 file MP3 cho 6 địa điểm x 4 ngôn ngữ, phát qua `GET /api/packages/stream`. | PRD FR-3.3 | P0 | Có file thật |
| F7 | Tải gói offline ZIP (`content.txt` + `audio.mp3`) qua `GET /api/packages/offline`. | PRD FR-4.1 | P0 | Có API |
| F8 | Phân cấp địa điểm cha–con qua `parent_id`, API `GET /api/locations/{id}/sub-locations`. | PRD FR-6 | P0 | Có API |
| F9 | Xác thực quản trị qua JWT (`POST /api/auth/login`, header `Authorization: Bearer`), API admin bị chặn nếu thiếu/sai token → 401. | PRD FR-7.1 | P0 | Có |
| F10 | Bảo vệ quyền riêng tư du khách: không thu thập họ tên/SDT/email, chỉ `deviceId` UUID ẩn danh. | PRD FR-7.2 | P0 | Có |
| F11 | Dashboard admin: tổng lượt nghe, thời gian trung bình, tỷ lệ kích hoạt, Top 5 địa điểm. | PRD FR-5.3 | P0 | Có dữ liệu thật |
| F12 | Giám sát phiên nghe trực tiếp: deviceId, địa điểm, ngôn ngữ, thời lượng, trigger type. | PRD FR-5.2 | P0 | Có |
| F13 | Quản trị địa điểm: CRUD, ẩn/hiện (không xóa dây chuyendu), validation, conflict rõ ràng. | PRD, Mục D/E | P0 | Có API, cần test trên DB thử |
| F14 | Quản trị nội dung và audio: tạo/sửa/xóa bài theo ngôn ngữ, TTS thật, giữ audio cũ khi sinh mới. **Chốt nghiệp vụ: mỗi địa điểm/ngôn ngữ chỉ có một bài thuyết minh; không triển khai hỗ trợ nhiều phiên bản trong phạm vi đồ án.** Khi sửa văn bản, audio cũ được giữ lại và văn bản mới có thể khác audio (giới hạn đồng bộ cần ghi rõ). | PRD, Mục D/E | P0 | Có API, cần test trên DB thử |
| F15 | Quản trị QR: tạo/cập nhật mã, xem trước, tải PNG. | PRD, Mục E | P1 | Có UI, cần quét thật |

### 2.2 An toàn database và bảo mật cơ bản (P0)

| # | Tiêu chí | Mức | Trạng thái mục tiêu |
|---|----------|-----|---------------------|
| S1 | Cách li database test: instance riêng, tài khoản chỉ quyền trên 2 database test, không dùng database ứng dụng. | P0 | Xác minh `SHOW GRANTS` |
| S2 | Migration `V4__location_visibility.sql` chạy được trên database thử (`audiotour_test`/`audiotour_prod_profile_test`); `docs/ROLLBACK.md` đã có hướng dẫn phục hồi. | P0 | Chạy V4 trên database thử, đối chiếu Flyway |
| S2b | Phục hồi backup và chạy artifact cũ trên database trống riêng (rollback rehearsal). | P1 | Diễn tập theo `docs/ROLLBACK.md` |
| S3 | `ddl-auto: validate`, Flyway baseline, ràng buộc FK trên InnoDB. | P0 | Đã cấu hình |
| S4 | JWT: từ chối secret/mật khẩu dev trên profile prod, thu hồi JWT cũ khi đổi mật khẩu, không lưu token localStorage dài hạn. | P0 | Đã code, cần verify trên prod profile |
| S5 | Validation/conflict: dữ liệu không hợp lệ trả thông báo rõ, không lộ raw SQL. | P0 | Có test handler |

### 2.3 Kiểm thử tích hợp luồng chính (P0)

| # | Tiêu chí | Mức | Trạng thái mục tiêu |
|---|----------|-----|---------------------|
| T3 | Test tích hợp: ẩn/hiện, cập nhật cha–con đồng thời, QR trùng, JWT, dữ liệu Tourist, lịch sử nghe. | P0 | **Đã chạy, đạt** — 12 integration test (Auth, LocationVisibility, LocationCreate/Update/Delete, AdminNarrationWrite/List, LocationHierarchyConcurrency, Pagination, Language, SeedDataIntegrity, ProdProfile), 0 failures trong `target/surefire-reports/`. Không chạy lại nếu không có thay đổi liên quan. |
| T4 | Frontend `npm test` đạt, `npm run build` đạt, `npm audit --omit=dev` 0 lỗ hổng đã biết. | P0 | Passed (44/44, 48/48) |
| T5 | `./mvnw.cmd compile -q` đạt; JAR đóng gói đúng asset frontend. | P0 | Passed |

### 2.4 Khả năng demo thực tế (P0)

| # | Tiêu chí | Mức | Trạng thái mục tiêu |
|---|----------|-----|---------------------|
| M1 | JAR build được, chạy với profile prod trên cổng 18081, kết nối database test. | P0 | Passed |
| M2 | Trên JAR: Home, đăng nhập admin, địa điểm, biểu đồ, QR, refresh cài đặt, API admin thiếu JWT trả 401. | P0 | Partial (cần mở rộng luồng Tourist/Admin, audio, phân quyền mọi route) |
| M3 | Demo trực tuyến/refresh, điều hướng, JWT, luồng Tourist/Admin, audio và ZIP trên database test. | P0 | Chưa đầy đủ → phải chạy |

### 2.5 Chất lượng sản xuất đã hạ mức (P1/P2, không tự chặn)

| # | Tiêu chí gốc | Mức mới | Lý do hạ mức |
|---|--------------|---------|---------------|
| Q1 | Docker build và GitHub Actions thực tế có bằng chứng hai gate đạt. | P1 | Máy không có Docker; đồ án đại học không yêu cầu triển khai container thật. Commit CI cũ không thay thế CI hiện hành. |
| Q2 | Multi-instance, rate limit dùng chung, bộ đếm shared. | P2 | Chỉ cần rate limit riêng instance cho demo đồ án. |
| Q3 | Rollback rehearsal trên database production thật. | P2 | Diễn tập trên bản sao test là đủ cho đồ án. |
| Q3b | Tương thích artifact cũ và kiểm thử trên mọi thiết bị. | P2 | Không cần trong phạm vi demo đồ án. |
| Q4 | Kiểm thử toàn diện mọi thiết bị (điện thoại thật, HTTPS, GPS thực địa, camera QR, autoplay). | P1 | Khuyên làm để demo tốt, không chặn nếu không có thiết bị. |
| Q5 | Responsive đầy đủ 375/768/1024/1440px, screen reader, reduced motion, mọi trang. | P1 | Có kiểm tra một phần, không chặn nếu đã demo được luồng chính. |
| Q6 | Uptime ≥ 99.9%, chịu tải 200 kết nối đồng thời, script `test_4_users.ps1`. | P2 | Quá khắt khe với phạm vi đồ án môn học. |
| Q7 | Chuyển route mặc định, xóa HTML cũ. | P1 | Chỉ sau khi các điểm kiểm tương ứng đạt và người dùng chấp nhận. |

---

## 3. Danh sách việc còn lại (theo mức ưu tiên)

### P0 — phải làm trước khi chốt

1. **Cách li database test** (mục D, S1): tạo MySQL test riêng (`127.0.0.1:3306`), tài khoản `audiotour_test` chỉ quyền trên 2 database test, xác minh `SHOW GRANTS`, không dùng database ứng dụng. Không chạy lại các test đã Passed nếu không có thay đổi liên quan.
2. **Migration V4 chạy trên database thử** (mục D, S2): chạy `V4__location_visibility.sql` trên database thử (`audiotour_test`), đối chiếu dữ liệu và lịch sử Flyway. Không áp dụng lên database ứng dụng trước khi backup.
3. **Kiểm thử luồng quản trị trên database test** (mục E, M2/M3): đăng nhập, 401/429, token hết hạn, đổi mật khẩu, thu hồi token cũ, tạo/sửa/xóa, ẩn/hiện, validation, conflict. Tuyệt đối không ghi dữ liệu production.
4. **Kiểm tra artifact hiện hành** (mục F): chạy JAR với database test, kiểm tra URL trực tiếp/refresh, điều hướng, JWT, luồng Tourist/Admin, audio và ZIP.
5. **Chốt phát hành** (mục F): lưu bằng chứng và giới hạn đã chấp nhận, mới cân nhắc chuyển route mặc định.

### P1 — khuyên làm, không chặn nếu không có điều kiện

6. Diễn tập migration và rollback V4 (phục hồi backup, chạy artifact cũ) trên database trống riêng theo `docs/ROLLBACK.md`.
7. Kiểm tra thực địa điện thoại/HTTPS: GPS, camera QR, autoplay, host QR truy cập được.
8. Kiểm tra Docker build và GitHub Actions trên commit chứa thay đổi mới.
9. Kiểm tra QR bằng điện thoại thực tế (tải PNG, quét mã).
10. Kiểm tra dịch/TTS với dịch vụ thật, xác nhận chi phí trước khi gọi.
11. Kiểm tra giao diện admin từng trang: desktop/mobile, focus, bàn phím, screen reader cơ bản, console/network.
12. Kiểm tra biểu đồ và phiên với dữ liệu thử nghiệm: bộ lọc ngày, khoảng rỗng, phiên phát/tạm dừng/rời phiên.
13. Đối chiếu HTML cũ với React trước khi chuyển route hoặc xóa file cũ.

### P2 — không làm trước khi P0 hoàn thành

14. Multi-instance, rate limit dùng chung, bộ đếm shared.
15. Rollback rehearsal trên database production thật.
16. Kiểm thử toàn diện mọi thiết bị và viewport.
17. Chịu tải 200 kết nối đồng thời, uptime 99.9%.
18. Tương thích artifact cũ và kiểm thử trên mọi thiết bị.

---

## 4. Điều kiện chốt đồ án (Definition of Done)

Toàn bộ điều kiện sau phải thỏa mãn (hoặc có Accepted Limitation được ghi nhận rõ ràng):

- [ ] Tất cả tiêu chí **P0** trong phần 2 đạt hoặc được ghi nhận Accepted Limitation.
- [ ] Không còn lỗi nghiêm trọng ở luồng chính (đăng nhập, dashboard, CRUD địa điểm, audio, ZIP, JWT 401).
- [ ] Database test đã cách li, `SHOW GRANTS` xác nhận, không dùng database ứng dụng.
- [ ] Backend suite đạt trên database test (172 test), frontend `npm test` đạt, `npm run build` đạt.
- [ ] Migration V4 đã chạy trên database thử, đối chiếu Flyway. (Rollback rehearsal là P1, không bắt buộc chốt.)
- [ ] JAR chạy được với profile prod, demo trực tuyến/refresh, đăng nhập admin, API thiếu JWT trả 401.
- [ ] Mọi giới hạn còn lại (P1/P2 chưa làm) được ghi nhận minh bạch trong `audio-tour-open-items.md`.
- [ ] Không chuyển route mặc định và không xóa HTML cũ trừ khi các điểm kiểm tương ứng đạt và người dùng chấp nhận.

P1/P2 không tự động chặn nghiệm thu, trừ khi được yêu cầu trong rubric môn học.

---

## 5. Tóm tắt tiêu chí đã hạ mức và lý do

| Tiêu chí gốc | Mức cũ | Mức mới | Lý do |
|--------------|--------|---------|-------|
| Docker build + GitHub Actions có bằng chứng | Production-grade | **P1** | Máy không có Docker; đồ án đại học không yêu cầu deploy container thật. |
| Multi-instance, rate limit shared, bộ đếm dùng chung | Production-grade | **P2** | Chỉ cần rate limit riêng instance cho demo đồ án. |
| Rollback rehearsal trên database production thật | Production-grade | **P2** | Diễn tập trên bản sao test là đủ cho đồ án. |
| Tương thích artifact cũ và kiểm thử mọi thiết bị | Production-grade | **P2** | Không cần trong phạm vi demo đồ án. |
| Kiểm thử toàn diện mọi thiết bị (điện thoại thật, HTTPS, GPS, camera QR, autoplay) | Production-grade | **P1** | Khuyên làm để demo tốt, không chặn nếu thiếu thiết bị. |
| Responsive đầy đủ mọi viewport, screen reader, reduced motion | Production-grade | **P1** | Có kiểm tra một phần, không chặn nếu luồng chính demo được. |
| Uptime ≥ 99.9%, chịu tải 200 kết nối đồng thời | Production-grade | **P2** | Quá khắt khe với phạm vi đồ án môn học. |
| Chuyển route mặc định, xóa HTML cũ | Production-grade | **P1** | Chỉ sau khi các điểm kiểm đạt và người dùng chấp nhận. |

---

## 6. Vấn đề thực sự đang chặn nghiệm thu (P0, chưa giải quyết)

| STT | Vấn đề | Loại | Bằng chứng từ source code / kiểm thử | Hành động cần làm |
|-----|--------|------|--------------------------------------|-------------------|
| 1 | Cách li database test chưa verify trên MySQL thật | Thiếu bằng chứng | `TestDatabaseIsolation.java` + `TestDatabaseIsolationTest` (8 tests, 0 failures) đã triển khai cách li sang `127.0.0.1:3306/audiotour_test` và `audiotour_prod_profile_test`; đã chạy surefire unit test (5/5, 0 failures), đã kết nối MySQL cổng 3306 và xác minh `SHOW GRANTS` | Đã hoàn tất: MySQL chạy cổng 3306, `SHOW GRANTS` xác nhận quyền chỉ trên 2 database test |
| 2 | Migration `V4__location_visibility.sql` chưa chạy trên database thử | Chưa triển khai trên DB | File migration đã tồn tại (visible DEFAULT TRUE, bảng `location_hierarchy_lock`); đã chạy thành công trên `audiotour_test` (cổng 3306), cột `visible` DEFAULT TRUE và bảng `location_hierarchy_lock` đã tạo, đã ghi vào `flyway_schema_history` version 4 success | Đã hoàn tất: V4 đã chạy và đối chiếu Flyway trên database thử |
| 3 | Quy tắc nghiệp vụ: mỗi địa điểm/ngôn ngữ chỉ một bài thuyết minh chưa ghi chốt | Quyết định nghiệp vụ | API hiện chỉ 1 bài mỗi địa điểm/ngôn ngữ; chưa ghi rõ giới hạn đồng bộ văn bản-audio | Ghi chốt trong tài liệu, không triển khai nhiều phiên bản |
| 4 | Luồng quản trị chưa có bằng chứng thực tế đầy đủ (401/429, token hết hạn, validation/conflict) | Thiếu bằng chứng trên DB thật | 39 integration test đã chạy đạt trên database test thật `audiotour_test` (cổng 3306), bao gồm 401/429, token hết hạn, đổi mật khẩu, validation/conflict | Đã hoàn tất: 39 integration tests, 0 failures, 0 errors trên database test thật |
| 5 | Artifact JAR hiện hành chưa kiểm tra đầy đủ luồng Tourist/Admin, audio, ZIP, phân quyền mọi route | Thiếu bằng chứng | JAR đã build và chạy profile prod cổng 18081 (M1 Passed); chỉ kiểm tra một phần trang admin, chưa kiểm tra đầy đủ audio/ZIP/phân quyền | Chạy JAR với database test, kiểm tra URL trực tiếp/refresh, điều hướng, JWT, luồng Tourist/Admin, audio, ZIP |

**Điều kiện mở khóa:** giải quyết 5 vấn đề trên (mức P0) mới được coi là chốt nghiệm thu đồ án. P1/P2 có thể để lại làm sau hoặc ghi nhận là Accepted Limitation.
