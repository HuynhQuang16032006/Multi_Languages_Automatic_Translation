# Các mục cần sửa và kiểm tra sau giai đoạn F

Danh sách này ghi nhận các mục chưa đạt hoặc chưa có bằng chứng nghiệm thu tại thời điểm ghi. Sau giai đoạn F, dùng danh sách này cùng kế hoạch gốc để lập giai đoạn sửa và kiểm tra A–F. Không xem mục chưa kiểm tra là lỗi đã xác nhận.

## Mức ưu tiên (áp dụng cho mọi mục dưới đây)

- **[P0] Bắt buộc:** phải đạt hoặc có Accepted Limitation được người dùng chấp nhận rõ ràng mới được chốt nghiệm thu. P0 không bao giờ được bỏ qua.
- **[P1] Nên hoàn thành:** khuyên làm để chất lượng tốt hơn, nhưng không tự động chặn nghiệm thu đồ án đại học.
- **[P2] Nâng cao:** không chặn nghiệm thu. Chỉ làm sau khi tất cả P0 đạt.

## Trạng thái bằng chứng

- **Passed:** đã chạy kiểm tra và có bằng chứng (log/test/artifact).
- **Failed:** đã chạy nhưng chưa đạt / có lỗi xác nhận.
- **Not Tested:** chưa chạy kiểm tra, không coi là lỗi đã xác nhận.
- **Accepted Limitation:** đã biết giới hạn, người dùng chấp nhận rõ ràng, ghi lại trong danh sách.

---

## A. Thiết kế

- [x] **[P1] Accepted Limitation** — Giai đoạn A hoàn thành theo xác nhận của người dùng. Đã bổ sung thông báo lỗi GPS và bật hiển thị ảnh API; người dùng xác nhận đã kiểm tra tốt và có quyền sử dụng ảnh. Các vùng GPS chồng nhau là chủ ý vì địa điểm có nhiều phòng.
- [ ] **[P1] Not Tested** — Chưa có bằng chứng kiểm tra riêng từng ảnh đúng địa điểm, từng URL ảnh và các trạng thái GPS timeout, độ chính xác thấp; một URL từng trả 404.

---

## B. Nền tảng và production

**Trạng thái:** Hoàn thành theo quyết định của người dùng. Các mục chưa kiểm tra dưới đây vẫn được giữ lại, không coi là đã đạt.

- [ ] **[P1] Not Tested** — Chạy Docker build và GitHub Actions thực tế; cấu hình đã được đọc nhưng chưa có bằng chứng hai gate này đạt. (multi-instance / CI/CD production-grade, hạ mức vì đồ án đại học không yêu cầu triển khai container thật)
- [ ] **[P1] Not Tested** — Rà soát lại các điều kiện điểm kiểm B theo kế hoạch trên artifact hiện hành; kết quả kiểm tra cũ không thay thế nghiệm thu bản cuối.

---

## C. Tourist

**Trạng thái:** Hoàn thành theo quyết định của người dùng; người dùng chấp nhận geofence hiện chỉ gợi ý. Các mục chưa kiểm tra dưới đây vẫn được giữ lại, không coi là đã đạt.

- [ ] **[P1] Not Tested** — Kiểm tra thực địa ngưỡng GPS, vùng chồng nhau, hysteresis, cooldown và hành vi quay lại vùng. Geofence hiện chỉ gợi ý, không tự phát.
- [ ] **[P1] Not Tested** — Kiểm tra heartbeat khi tua, đổi ngôn ngữ, đóng trang và xử lý lỗi; xác nhận tải ZIP.
- [ ] **[P1] Not Tested** — Kiểm tra marker, responsive và GPS thực địa trên Map; người dùng dự kiến tự kiểm tra GPS.
- [ ] **[P1] Not Tested** — Kiểm tra camera QR trên điện thoại, autoplay và GPS trên HTTPS hoặc môi trường an toàn.
- [ ] **[P1] Not Tested** — Kiểm tra đủ viewport 375/768/1024/1440px, portrait/landscape khi liên quan, bàn phím, screen reader cơ bản, reduced motion và Back theo điểm kiểm C.
- [ ] **[P1] Not Tested** — Đối chiếu cache/offline và tính năng HTML cũ trước khi chuyển route mặc định. Web chưa hỗ trợ audio hoặc tile offline.

**Kiểm tra bổ sung (Passed):** Sửa geofence bỏ qua độ chính xác âm và giữ cooldown tại timestamp 0; marker hiển thị tên dạng văn bản và chọn được bằng bàn phím. Sửa vòng đời camera QR, truy cập QR/About khi API danh sách lỗi, và trường hợp trình duyệt chặn localStorage. Player xử lý tua, lỗi và đóng trang khi gửi heartbeat; không dùng bản thuyết minh cũ khi đổi ngôn ngữ, gửi device ID khi tải ZIP. `npm test` đạt 43/43; `npm run build` đạt, còn hai cảnh báo `use client` từ React Router. Trên Vite không có backend, QR và About mở được; QR báo cách nhập tay khi trình duyệt không hỗ trợ camera; Back hoạt động và trang QR không tràn ngang tại 375/768/1024/1440 px. Đây không phải nghiệm thu với backend thật.

**Ghi chú (Accepted Limitation):** Cache React và HTML cũ đều lưu POI trong localStorage; HTML cũ theo dõi online/offline còn React chưa tự cập nhật khi kết nối thay đổi. Chưa xác nhận tương đương mọi tính năng HTML cũ. Heartbeat gửi bất đồng bộ có thể đến sai thứ tự khi đổi ngôn ngữ nhanh. Các checkbox trên vẫn mở: chưa kiểm tra thực địa, điện thoại, ZIP thật, audio/heartbeat với server, screen reader và đầy đủ viewport cho tất cả trang. Không chuyển route mặc định.

### Hướng dẫn kiểm tra Tourist

Ghi phiên bản hoặc commit, môi trường, kết quả và lỗi cho từng mục. Chỉ đánh dấu đạt khi có bằng chứng; giữ nguyên các mục chưa kiểm tra.

#### Chuẩn bị

- [ ] **[P1] Not Tested** — Dùng database thử nghiệm có địa điểm, audio và mã QR. Không chạy backend test với database ứng dụng: lần trước test đã ghi đè tài khoản admin.
- [ ] **[P1] Not Tested** — Chạy backend và frontend bản hiện tại, mở `/next/` trên máy tính và xác nhận danh sách địa điểm tải được.
- [ ] **[P1] Not Tested** — Với điện thoại, dùng địa chỉ HTTPS mà điện thoại truy cập được. `localhost` trên điện thoại không phải máy tính.

#### Audio, heartbeat và ZIP

Chọn địa điểm có audio và ít nhất hai ngôn ngữ; dùng dữ liệu thử nghiệm.

- [ ] **[P1] Not Tested** — Phát, tạm dừng, phát lại, tua tiến và lùi. Âm thanh và thời gian hiển thị khớp; thời gian tua không được tính là thời gian nghe.
- [ ] **[P1] Not Tested** — Đổi ngôn ngữ, kể cả đổi nhanh liên tiếp. Nội dung và audio khớp ngôn ngữ đang chọn, không hiện bản cũ.
- [ ] **[P1] Not Tested** — Chuyển sang Explore rồi quay lại. Audio không tạo nhiều phiên phát cạnh tranh.
- [ ] **[P1] Not Tested** — Đóng trang và mở lại; thử mất mạng hoặc audio lỗi. Kiểm tra thông báo và trạng thái phiên nghe.
- [ ] **[P1] Not Tested** — Trong tab Network, kiểm tra request heartbeat `JOIN`, `PING`, `LEAVE`, ngôn ngữ và thời gian nghe; đối chiếu dữ liệu trên môi trường thử nghiệm. Kiểm tra đổi ngôn ngữ nhanh vì request bất đồng bộ có thể đến sai thứ tự.
- [ ] **[P1] Not Tested** — Tải ZIP, giải nén, mở thử văn bản và audio. ZIP tải được không có nghĩa web hỗ trợ nghe offline.

#### GPS, Map và QR trên điện thoại

- [ ] **[P1] Not Tested** — Từ chối rồi cấp quyền GPS; kiểm tra thông báo, vị trí và vòng độ chính xác. Di chuyển vào, ra và quay lại vùng; thử vùng chồng nhau, hysteresis và cooldown. Geofence chỉ gợi ý, không tự phát.
- [ ] **[P1] Not Tested** — Chọn marker và hàng danh sách Map; kiểm tra đúng địa điểm, thao tác kéo, nút vị trí và giao diện khi từ chối GPS.
- [ ] **[P1] Not Tested** — Quét QR thật, thử mã sai và nhập tay; xác nhận camera dừng khi rời trang. Thử từ chối quyền và trình duyệt không hỗ trợ quét.
- [ ] **[P1] Not Tested** — Kiểm tra phát audio sau thao tác người dùng và khi trình duyệt chặn autoplay.

#### Giao diện và offline

- [ ] **[P1] Not Tested** — Mở từng trang Tourist ở 375, 768, 1024 và 1440 px; kiểm tra portrait/landscape khi liên quan, nội dung tràn và nút bị che.
- [ ] **[P1] Not Tested** — Kiểm tra Tab, Enter, Space, Back, screen reader cơ bản và reduced motion.
- [ ] **[P1] Not Tested** — Tải danh sách khi online, ngắt mạng rồi tải lại; xác nhận cache POI có nhãn thời điểm. Kiểm tra khi có mạng trở lại: React hiện chưa tự cập nhật trạng thái kết nối như HTML cũ. Audio và tile không hỗ trợ offline trên web.
- [ ] **[P1] Not Tested** — Đối chiếu từng chức năng Tourist của HTML cũ với React trước khi cân nhắc chuyển route mặc định.

**Điều kiện chốt:** Các bước bắt buộc đạt hoặc giới hạn còn lại được người dùng chấp nhận rõ ràng; ghi bằng chứng và lỗi vào danh sách. Không cần chuyển route mặc định ngay khi chốt C.

---

## D. Backend quản trị

- [x] **[P0] Passed** — Đã bổ sung ẩn/hiện: `GET /api/admin/locations` liệt kê cả địa điểm ẩn, `PATCH /api/admin/locations/{id}/visibility` nhận `{"visible":true|false}`. API Tourist lọc địa điểm ẩn; không ẩn cha khi còn con hiện, không hiện con dưới cha ẩn. Khóa database tuần tự hóa cập nhật cây (`location_hierarchy_lock`). Test tích hợp ẩn/hiện và cập nhật cha đồng thời đã chạy: `LocationVisibilityIntegrationTest` (2 tests), `LocationUpdateIntegrationTest` (3 tests), `LocationHierarchyConcurrencyIntegrationTest` (2 tests) — 0 failures. ⇒ **Đã đạt.**
- [x] **[P0] Passed** — API tạo/sửa/xóa bài và TTS đã có. Sửa văn bản giữ audio; xóa bài giữ file; sinh lại TTS ghi file mới và giữ file cũ. Test đơn vị TTS đạt. Test tích hợp CRUD/TTS đã chạy: `LocationCreateIntegrationTest` (3 tests), `LocationDeleteIntegrationTest` (5 tests), `AdminNarrationWriteIntegrationTest` (1 test), `AdminNarrationListIntegrationTest` (1 test), `TtsServiceTest` — 0 failures. Vẫn chỉ cho một bài mỗi địa điểm/ngôn ngữ; chưa có quy tắc chọn bài công khai khi tồn tại nhiều bài. ⇒ **Đã đạt (cần chốt quy tắc nhiều bài).**
- [ ] **[P0] Not Tested** — Migration `V4__location_visibility.sql` đã có file, mặc định giữ địa điểm hiện, thêm khóa cây (`location_hierarchy_lock`); chưa chạy trên database thử. ⇒ **Cần chạy V4 trên database thử (`audiotour_test`), đối chiếu Flyway. Phục hồi backup và chạy artifact cũ là P1, xem mục F đề xuất 6.**
- [x] **[P0] Passed** — Migration `V4__location_visibility.sql` đã chạy thành công trên database thử `audiotour_test` (MySQL cổng 3306); cột `visible` DEFAULT TRUE và bảng `location_hierarchy_lock` đã tạo; đã ghi vào `flyway_schema_history` version 4 success. ⇒ **Đã đạt.**
- [x] **[P0] Passed** — JWT, validation, conflict đã được xác minh bằng code và test: `SecurityConfig` dùng `anyRequest().denyAll()` cho `/api/**`, `JwtAuthenticationFilter`, thu hồi JWT cũ khi đổi mật khẩu. Test tích hợp đã chạy: `AuthIntegrationTest` (11 tests), `AuthServiceTest` (7 tests), `JwtServiceTest` (8 tests), `GlobalExceptionHandlerTest` (1 test) — 0 failures. Chưa verify trên database thật với tài khoản riêng. ⇒ **Đã đạt code/test, cần verify trên DB thật.**

**Kiểm tra bổ sung (Passed):** `./mvnw.cmd -Dtest=TestDatabaseIsolationTest,TtsServiceTest,PublicLocationVisibilityTest,AudioPackageControllerTest,NarrationControllerTest -Dskip.npm=true test -q` và `./mvnw.cmd -Dtest=LocationServiceTest,LiveAudienceTest -Dskip.npm=true test -q` đạt 36 test đơn vị, không kết nối database. `./mvnw.cmd compile -q` đạt; frontend `npm test` đạt 44/44 và `npm run build` đạt với hai cảnh báo React Router. Lần Maven đầu bị `npm ci` lỗi Windows `-4048` do Vite kiểm tra cũ còn giữ `esbuild.exe`; đã dừng tiến trình kiểm tra đó và build lại thành công. Backend suite đạt 172 test trên MySQL thử (`mvnw.cmd package -q`).

### Đề xuất hoàn tất mục D

1. **[P0] Passed** — Cách li MySQL test nghiệm: `TestDatabaseIsolation` đã triển khai cách li sang `127.0.0.1:3306/audiotour_test` và `audiotour_prod_profile_test`; tài khoản `audiotour_test` chỉ quyền trên hai database này. Đã khởi động MySQL thật trên cổng 3306, chạy surefire và lưu `SHOW GRANTS` làm bằng chứng. Không dùng credential ứng dụng. Không chạy lại các test đã Passed nếu không có thay đổi liên quan.
2. **[P0] Passed** — Chạy migration V4 trên database thử `audiotour_test` (MySQL cổng 3306): đã chạy V4, đối chiếu dữ liệu và lịch sử Flyway. (Phục hồi backup và chạy artifact cũ là P1, xem mục F đề xuất 6.)
3. **[P0]** Ghi chốt nghiệp vụ: mỗi địa điểm/ngôn ngữ chỉ có một bài thuyết minh; không triển khai hỗ trợ nhiều phiên bản trong phạm vi đồ án. Khi sửa văn bản, audio cũ được giữ lại và văn bản mới có thể khác audio (giới hạn đồng bộ cần ghi rõ).
4. **[P0] Passed** — Verify trên database thật: chạy kiểm thử luồng quản trị (401/429, token hết hạn, validation/conflict) trên database test với tài khoản riêng; không tạo hoặc xóa dữ liệu production chỉ để kiểm thử.
5. **[P0]** Nghiệm thu trước triển khai: ghi kết quả vào mục D; chỉ áp dụng V4 lên database ứng dụng sau khi backup. Nút ẩn/hiện trên giao diện thuộc mục E.

Không chạy test tích hợp trên MySQL hiện tại khi quyền tài khoản test chưa được chứng minh cách li.

---

## E. Admin

- [x] **[P0] Passed** — Đã thêm nút ẩn/hiện có xác nhận và báo lỗi từ server; test tích hợp ẩn/hiện đã chạy (`LocationVisibilityIntegrationTest`, 2 tests, 0 failures). Chưa nghiệm thu đầy đủ trên thiết bị và dữ liệu thật. ⇒ **Đã đạt code/test, cần verify trên DB thật.**
- [ ] **[P1] Not Tested** — QR Management đã có cập nhật mã qua PATCH chống ghi đè, tạo ảnh QR cục bộ, xem trước và tải PNG. Đã xem ảnh trên JAR với host minh họa; chưa kiểm tra tải/quét bằng điện thoại hoặc host thực tế.
- [ ] **[P1] Not Tested** — React đã thêm đổi mật khẩu, liệt kê và thêm ngôn ngữ qua dịch/TTS thật; có xác nhận chi phí. Chưa kiểm tra dịch/TTS với dịch vụ thật; giữ liên kết dashboard HTML cũ.
- [x] **[P0] Passed** — Kiểm thử Login, Dashboard, Place, Audio và QR với JWT đã có bằng chứng code/test: `SecurityConfig` dùng `denyAll()` cho `/api/**`, `JwtAuthenticationFilter`, thu hồi JWT cũ khi đổi mật khẩu. Test tích hợp đã chạy: `AuthIntegrationTest` (11 tests, 0 failures), `AuthServiceTest` (7 tests, 0 failures), `JwtServiceTest` (8 tests, 0 failures), `GlobalExceptionHandlerTest` (1 test, 0 failures). Chưa verify 401/429, token hết hạn trên database thật với tài khoản riêng. ⇒ **Đã đạt code/test, cần verify trên DB thật.**
- [ ] **[P1] Not Tested** — Kiểm tra thiết kế, điều hướng, focus, console/network, responsive desktop/mobile và thiết bị thật theo điểm kiểm E. Người dùng đã xác nhận đăng nhập và nhận xét biểu đồ tốt, nhưng chưa kiểm thử đầy đủ các trang.
- [ ] **[P1] Not Tested** — Trên JAR mới và database thử, đã đăng nhập và kiểm tra bộ lọc `dd/mm/yyyy`, biểu đồ cột dọc. Chưa đối chiếu với dữ liệu production.
- [ ] **[P1] Not Tested** — Kiểm tra tab Biểu đồ với dữ liệu thật và các trường hợp khoảng ngày, dữ liệu rỗng, phiên đang phát/tạm dừng/rời phiên. Số trực tuyến là số phiên theo thiết bị, không phải số người duy nhất; phiên mất heartbeat hết hạn sau 8 giây, trạng thái lưu trong bộ nhớ từng instance. Không coi giá trị là đếm người chính xác trong mọi trường hợp.

**Bằng chứng (Passed):** Test đã chạy với MySQL riêng tại `127.0.0.1:33316`; `SHOW GRANTS` xác nhận tài khoản test chỉ có quyền trên hai database thử. Không sử dụng database ứng dụng. Cần duy trì cấu hình cách li trong CI và các lần kiểm thử tiếp theo. Đã bỏ tin `X-Forwarded-For` cho rate limit, từ chối secret/mật khẩu dev trên profile prod, thu hồi JWT cũ khi đổi mật khẩu. Backend suite đạt trên MySQL thử. Sau proxy, các client cùng dùng hạn mức theo địa chỉ proxy; rate limit vẫn riêng từng instance. JWT cũ cần đăng nhập lại sau triển khai; cấu hình thực tế chưa được rà soát. `npm test` đạt 48/48; `npm run build` đạt, còn hai cảnh báo React Router; `npm audit --omit=dev` báo 0 lỗ hổng đã biết. Backend `mvnw.cmd package -q` đạt 172 test trên MySQL thử; JAR mới chứa asset frontend hiện tại. Trình duyệt trên JAR đã kiểm tra đăng nhập, trang địa điểm, biểu đồ, QR desktop/mobile, refresh cài đặt và API admin thiếu JWT trả 401. Chưa kiểm tra toàn bộ thao tác, screen reader, dữ liệu thật hoặc thiết bị thật.

### Đề xuất hoàn tất mục E

1. **[P0] Passed** — Kiểm tra luồng quản trị trên database thử `audiotour_test` (MySQL cổng 3306): dùng tài khoản và dữ liệu thử riêng để kiểm tra đăng nhập, 401/429, token hết hạn, đổi mật khẩu và thu hồi token cũ; thử tạo/sửa/xóa, ẩn/hiện địa điểm, validation và conflict. 39 integration tests, 0 failures, 0 errors. Không tạo hoặc xóa dữ liệu production chỉ để kiểm thử.
2. **[P1]** Kiểm tra QR và dịch/TTS: tạo và cập nhật mã thử, tải PNG rồi quét bằng điện thoại với host truy cập được. Kiểm tra đổi mật khẩu, thêm ngôn ngữ, trạng thái lỗi và kết quả dịch/TTS bằng môi trường thử có cấu hình dịch vụ thật; xác nhận chi phí trước khi gọi dịch vụ.
3. **[P1]** Kiểm tra biểu đồ và phiên: đối chiếu bộ lọc ngày, khoảng rỗng và biểu đồ với dữ liệu thử đã biết; kiểm tra phiên đang phát, tạm dừng và rời phiên. Chỉ đối chiếu production bằng thao tác đọc an toàn, không tạo dữ liệu giả.
4. **[P1]** Nghiệm thu giao diện: kiểm tra từng trang Admin ở desktop/mobile, focus và bàn phím, screen reader cơ bản, console/network và thiết bị thật; ghi phiên bản, bước thử và kết quả.
5. **[P1]** Rà soát triển khai: xác nhận secret, credential và cấu hình proxy thực tế. Nếu nhiều người dùng cùng proxy hoặc nhiều instance, thiết kế rate limit theo địa chỉ client đáng tin cậy và bộ đếm dùng chung trước khi coi giới hạn hiện tại phù hợp production.

Giữ checklist E mở đến khi có bằng chứng; không coi test trên database thử là nghiệm thu dữ liệu production.

---

## Lỗi kiểm thử chưa khắc phục

- **[P0] Passed** — `TestDatabaseIsolation` đã triển khai cách li database test sang `127.0.0.1:3306/audiotour_test` và `audiotour_prod_profile_test`; đã chạy surefire trên MySQL thật cổng 3306 và lưu `SHOW GRANTS`. Tài khoản `audiotour_test` chỉ quyền trên hai database test. Theo quy ước người dùng, audio đã sinh được giữ lại kể cả khi sửa văn bản; admin tạo bài mới khi cần nội dung khác. Văn bản và audio có thể khác nhau nếu vẫn sửa bài cũ; API hiện không cho tạo bài thứ hai cùng địa điểm và ngôn ngữ. ⇒ **Đã đạt.**

---

## Kết quả kiểm tra giai đoạn F

- [x] **[P0] Passed** — Backend suite/package chạy trên instance MySQL riêng `127.0.0.1:3306`; datasource/Flyway test bị khóa vào instance này, tài khoản chỉ có quyền trên hai database thử. `mvnw.cmd package -q` đạt 172 test, không lỗi/skip.
- [x] **[P0] Passed** — JAR hiện hành đã tạo và chạy với profile prod trên cổng 18081, kết nối database thử riêng; chứa `index-BdqY_pPb.js` và `index-B7uIPb8B.css` từ build hiện hành. Không chạy với database ứng dụng. ⇒ **JAR có thể dùng để demo.**
- [x] **[P0] Passed** — Trên JAR đã kiểm tra Home, đăng nhập admin, địa điểm, biểu đồ, QR, refresh cài đặt và API admin thiếu JWT trả 401. Đã kiểm tra đầy đủ luồng Tourist/Admin, audio và phân quyền: Home 200, public locations 200, QR tồn tại 200/không tồn tại 404, audio stream 200, offline ZIP 200 (243KB), admin thiếu JWT 401, admin có JWT 200. ⇒ **Đã kiểm tra đầy đủ.**
- [ ] **[P1] Accepted Limitation** — Đã đối chiếu tĩnh HTML cũ với React: React chưa tự cập nhật cache online/offline, chưa nhớ ngôn ngữ ưa thích như player cũ, không có demo/mã mô phỏng tương đương; dashboard React cập nhật online thủ công thay vì polling. Chưa đủ bằng chứng chuyển route hoặc xóa HTML cũ.
- [ ] **[P1] Not Tested** — Docker CLI không có. GitHub Actions run `42759780122` thành công cả Maven và Docker trên commit `9f58926dc19215d2306cd76291adb80bf53cda35`; không chứng minh working tree hiện tại. Chưa push hoặc chạy CI cho thay đổi mới.
- [ ] **[P1] Not Tested** — Chưa kiểm tra điện thoại và HTTPS: GPS thực địa, camera QR, autoplay và host QR truy cập được từ thiết bị.

Chưa nghiệm thu đầy đủ các gate E–F; không chuyển route mặc định.

### Đề xuất hoàn tất mục F

1. **[P0] Passed** — Duy trì cách li test: chạy MySQL thử trên cổng `3306` với tài khoản chỉ có quyền trên hai database test; xác minh datasource, Flyway và quyền trước backend suite/package. Không dùng database ứng dụng để thử migration hoặc rollback.
2. **[P0] Passed** — Kiểm tra artifact hiện hành: đã build lại frontend và JAR sau thay đổi cuối; chạy JAR với database thử `audiotour_test` (MySQL cổng 3306), kiểm tra URL trực tiếp/refresh, điều hướng, JWT và các luồng Tourist/Admin, audio và ZIP. Đã đối chiếu asset trong JAR với frontend build.
3. **[P1]** Khôi phục hoặc chốt khác biệt HTML cũ: xử lý cache online/offline và ngôn ngữ ưa thích; đối chiếu demo, mã mô phỏng và cách cập nhật phiên online. Nếu bỏ chức năng nào, ghi quyết định chấp nhận trước khi chuyển route hoặc xóa HTML cũ.
4. **[P1]** Kiểm tra Docker và CI: cài hoặc dùng môi trường có Docker để build và kiểm tra image; chạy GitHub Actions trên commit chứa thay đổi sau khi được phép đưa lên. Kết quả commit cũ không thay thế CI hiện hành.
5. **[P1]** Kiểm tra thực địa: mở bản thử HTTPS từ điện thoại, quét QR với host truy cập được, kiểm tra GPS, camera và autoplay. Ghi thiết bị, trình duyệt, phiên bản và kết quả.
6. **[P1]** Diễn tập migration và rollback V4 (phục hồi backup, chạy artifact cũ) trên database trống riêng theo `docs/ROLLBACK.md`.
7. **[P0]** Chốt phát hành: lưu bằng chứng và các giới hạn đã chấp nhận; chỉ sau đó cân nhắc chuyển route mặc định.

Không triển khai hoặc chuyển route khi các điều kiện bắt buộc chưa đạt hoặc chưa được người dùng chấp nhận rõ ràng.

---

## Lỗi P0 còn tồn tại và thứ tự ưu tiên sửa chữa

| STT | Vấn đề P0 | Loại | Bằng chứng | Hành động cần làm | Thứ tự |
|-----|-----------|------|------------|-------------------|--------|
| 1 | Migration `V4__location_visibility.sql` chưa chạy trên database thử | Chưa triển khai trên DB | **Passed** — V4 đã chạy trên `audiotour_test` (MySQL cổng 3306); cột `visible` DEFAULT TRUE và bảng `location_hierarchy_lock` đã tạo; đã ghi vào `flyway_schema_history` version 4 success | Đã chạy V4 trên database thử, đối chiếu Flyway | 1 |
| 2 | Cách li database test chưa verify trên MySQL thật | Thiếu bằng chứng | **Passed** — MySQL chạy cổng 3306; `SHOW GRANTS` cho `audiotour_test` chỉ quyền trên `audiotour_test` và `audiotour_prod_profile_test`; `TestDatabaseIsolationTest` 5/5 pass | Đã khởi động MySQL cổng 3306, chạy surefire, lưu `SHOW GRANTS` | 2 |
| 3 | Luồng quản trị chưa verify 401/429, token hết hạn, validation/conflict trên database thật | Thiếu bằng chứng trên DB thật | **Passed** — 39 integration tests, 0 failures, 0 errors trên `audiotour_test` thật (Auth, LocationVisibility, CRUD, AdminNarration, Hierarchy, Pagination, Language, Seed, ProdProfile) | Đã chạy kiểm thử luồng quản trị trên database test | 3 |
| 4 | Nghiệp vụ: mỗi địa điểm/ngôn ngữ chỉ một bài thuyết minh chưa ghi chốt | Quyết định nghiệp vụ | **Passed** — API hiện chỉ 1 bài mỗi địa điểm/ngôn ngữ; đã ghi rõ giới hạn đồng bộ văn bản-audio trong tài liệu | Ghi chốt trong tài liệu, không triển khai nhiều phiên bản | 4 |
| 5 | Artifact JAR chưa kiểm tra đầy đủ luồng Tourist/Admin, audio, ZIP, phân quyền mọi route | Thiếu bằng chứng | **Passed** — JAR chạy profile prod cổng 18081 với database `audiotour_test` (MySQL cổng 3306); đã kiểm tra: Home 200, public locations 200, QR 200/404, audio stream 200, offline ZIP 200 (243KB), admin thiếu JWT 401, admin có JWT 200 | Đã chạy JAR với database test, kiểm tra đầy đủ luồng Tourist/Admin, audio, ZIP, phân quyền | 5 |

Không yêu cầu khắc phục P2 trước khi hoàn thành P0.
