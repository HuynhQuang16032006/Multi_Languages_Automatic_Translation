# Task list: Trang chủ du khách + bản đồ (P7-1)

Kế hoạch đầy đủ: `tasks/plan.md`. Quy ước: đánh `[x]` khi đã verify. Lệnh verify chính: `.\mvnw.cmd clean test` (Windows) hoặc `./mvnw clean test`.

**Trạng thái: hoàn tất.** Branch `feature/guest-ui-map`. `.\mvnw.cmd clean verify` → 94 test, 0 fail, 0 error, BUILD SUCCESS (trước đợt này là 75 test).

### Kết quả verify bằng trình duyệt thật (app chạy thật + MySQL)

| Hạng mục | Kết quả quan sát |
|---|---|
| Trang chủ `/` | 6 POI trong danh sách, 6 marker trên bản đồ, tile tải về, 2 tour, console không lỗi |
| Marker + sheet | Bấm marker mở sheet: tên, địa chỉ, ảnh, 2 nút ngôn ngữ; focus vào panel; đóng được bằng nút X và Escape; focus quay lại card |
| POI thiếu ngôn ngữ | Trạm Khánh Hội + English → sheet hiện "Chưa có bản thuyết minh cho ngôn ngữ này tại điểm này." |
| "Gần tôi" | Có vị trí → khoảng cách 591 m / 601 m / 612 m / 1,70 km / 1,87 km / 2,05 km, danh sách sắp xếp lại, marker vị trí + vòng độ chính xác. Từ chối quyền → thông báo tiếng Việt, trang vẫn dùng được |
| Tour | Bấm "Tour Tuyến Xe Buýt Lịch Sử Quận 4" → còn đúng 3 POI, 3 marker nổi bật + 3 marker mờ, "Xem tất cả" khôi phục 6 POI |
| Player | `?locationId=5&lang=en` → text tiếng Anh trong DB, audio đúng POI; đổi ngôn ngữ đổi cả text lẫn audio; `?locationId=1&lang=en` → thông báo thiếu bản thuyết minh + báo lỗi audio |
| QR | `?qr=QR-PHONG-KHANH-TIET` → mở đúng POI 5; `?qr=QR-KHONG-TON-TAI` → "Không tìm thấy địa điểm theo mã QR này." + link về trang chủ, không phát nhầm POI mặc định |
| Offline | Mất mạng có cache → 6 POI từ `localStorage` + banner offline. Mất mạng không cache → banner lỗi + gợi ý tải gói offline, trang không trắng |

### Lệch khỏi kế hoạch

| Lệch | Lý do |
|---|---|
| T1 không sửa `SecurityConfig` | Dồn toàn bộ thay đổi `SecurityConfig` sang T2 để tránh 2 agent sửa cùng file. AC "không token vẫn 200" được cover ở `PublicReadAccessTest` |
| Thêm handler 400 cho thiếu/sai tham số | `MissingServletRequestParameterException` là checked exception, không rơi vào `handleRuntimeException` → thiếu param trả 500. Lỗi dùng chung, sửa ở `GlobalExceptionHandler` |
| Sửa 3 lỗi có sẵn phát hiện khi mở API public | `findByParentLocationId` thiếu `@EntityGraph` (400), nút ngôn ngữ `player.html` không đồng bộ với `?lang=`, QR sai ghi log phiên nghe nhầm POI 4 |
| Marker vừa có popup vừa mở sheet | AC của T4 (popup) và T5 (sheet) cùng áp dụng cho một cú click → giữ cả hai, popup vẫn mở phía sau sheet |
| `GuestHomePageTest` dùng `RANDOM_PORT` + JDK `HttpClient` cho `GET /` | MockMvc không forward, chỉ thấy `Forwarded URL = index.html` nên không kiểm tra được Content-Type |
| Không dựng test runner JS | Repo không có sẵn; UI verify bằng trình duyệt thật + `node --check` cho cú pháp |

---

## Task 1: `GET /api/narrations` trả text thuyết minh thật, public

**Mô tả:** Thêm endpoint đọc bài thuyết minh theo POI và ngôn ngữ. Endpoint này thay cho object `narrations` hardcode trong `player.html`.

**Acceptance criteria:**
- [x] `GET /api/narrations?locationId=5&languageCode=vi` trả 200 với `{ locationId, locationName, languageCode, textContent, audioUrl, durationInSeconds }`
- [x] `locationId` không tồn tại hoặc không có bản thuyết minh cho ngôn ngữ đó trả 404 với JSON chuẩn `{ timestamp, status, error, message, path }`
- [x] Thiếu `locationId` hoặc `languageCode` trả 400
- [x] Gọi không kèm token vẫn trả 200

**Verification:**
- [x] Test: `.\mvnw.cmd test -Dtest=NarrationControllerTest` xanh
- [x] Test: `.\mvnw.cmd clean test` xanh, không test cũ nào đỏ
- [x] Manual: `curl "http://localhost:8080/api/narrations?locationId=5&languageCode=vi"` trả JSON đúng `textContent` trong bảng `narrations`

**Dependencies:** Không

**Files:**
- `src/main/java/vn/edu/sgu/audiotour/dto/NarrationResponse.java` (mới)
- `src/main/java/vn/edu/sgu/audiotour/service/NarrationService.java` (mới)
- `src/main/java/vn/edu/sgu/audiotour/controller/NarrationController.java` (mới)
- `src/main/java/vn/edu/sgu/audiotour/config/SecurityConfig.java`
- `src/test/java/vn/edu/sgu/audiotour/NarrationControllerTest.java` (mới)

**Ghi chú kỹ thuật:** `open-in-view: false`, `Location.subLocations` lazy. Trả entity `Narration` sẽ kéo theo `Location` lồng nhau và gây `LazyInitializationException` lúc serialize. Vì vậy service dựng DTO, không trả entity. Tái dùng `NarrationRepository.findByLocationIdAndLanguageCode` đã có sẵn.

**Estimated scope:** Medium (5 files)

---

## Task 2: Mở public các `GET` đọc cho du khách

**Mô tả:** Cho phép trang chủ du khách đọc POI, tour, ngôn ngữ mà không cần token admin.

**Acceptance criteria:**
- [x] `GET /api/locations`, `GET /api/locations/pois`, `GET /api/locations/{id}`, `GET /api/locations/by-qr/{code}`, `GET /api/tours`, `GET /api/languages` trả 200 không token
- [x] `POST /api/languages`, `PUT /api/languages/{id}`, `DELETE /api/languages/{id}` không token vẫn trả 401
- [x] `POST /api/auth/login` không token trả 200 như trước

**Verification:**
- [x] Test: `.\mvnw.cmd test -Dtest=PublicReadAccessTest` xanh
- [x] Test: `.\mvnw.cmd clean test` xanh
- [x] Manual: `curl -i http://localhost:8080/api/locations/pois` → 200; `curl -i -X POST http://localhost:8080/api/languages` → 401

**Dependencies:** Task 1 (cùng sửa `SecurityConfig`)

**Files:**
- `src/main/java/vn/edu/sgu/audiotour/config/SecurityConfig.java`
- `src/test/java/vn/edu/sgu/audiotour/PublicReadAccessTest.java` (mới)

**Ghi chú kỹ thuật:** Dùng matcher `HttpMethod.GET` để chỉ mở phương thức đọc. Không đụng `POST/PUT/DELETE`.

**Estimated scope:** Small (2 files)

---

## Task 3: `index.html` + entry `/` + danh sách POI nối tới player

**Mô tả:** Dựng trang chủ du khách với khung layout mobile-first và danh sách POI lấy từ API, bấm POI để sang trang player.

**Acceptance criteria:**
- [x] `GET /` trả 200, `GET /index.html` trả 200
- [x] Trang hiện danh sách 6 POI lấy từ `/api/locations/pois`, mỗi item có tên, địa chỉ, ảnh
- [x] Bấm một POI đi tới `player.html?locationId={id}&lang=vi`
- [x] Layout 1 cột trên màn hình rộng, có `<meta name="viewport">`, màu chữ tương phản đạt WCAG AA
- [x] Khi API lỗi, trang hiện thông báo lỗi thay vì trắng

**Verification:**
- [x] Test: `.\mvnw.cmd test -Dtest=GuestHomePageTest` xanh (kiểm tra `/index.html` được serve và có chứa `api/locations/pois`)
- [x] Test: `.\mvnw.cmd clean test` xanh
- [x] Manual: mở `http://localhost:8080/` trên DevTools mobile viewport, thấy 6 POI, bấm thử đúng POI

**Dependencies:** Task 2

**Files:**
- `src/main/resources/static/index.html` (mới)
- `src/main/java/vn/edu/sgu/audiotour/config/SecurityConfig.java` (thêm matcher cho `/`)
- `src/test/java/vn/edu/sgu/audiotour/GuestHomePageTest.java` (mới)

**Estimated scope:** Medium (3 files)

---

## Task 4: Bản đồ Leaflet + marker POI

**Mô tả:** Thêm bản đồ Leaflet với tile OpenStreetMap, marker cho mỗi POI.

**Acceptance criteria:**
- [x] Leaflet 1.9 tải từ unpkg, tile OpenStreetMap hiện ra
- [x] 6 marker đúng toạ độ `latitude`/`longitude` từ API
- [x] Bản đồ tự `fitBounds` để vừa toàn bộ POI
- [x] Bấm marker hiện popup tên và địa chỉ
- [x] Bấm một item trong danh sách POI thì bản đồ pan tới marker đó
- [x] Console trình duyệt không có lỗi

**Verification:**
- [x] Manual: mở `http://localhost:8080/`, thấy bản đồ có 6 marker, bấm marker hiện popup
- [x] Manual: DevTools console không lỗi, Network không có request 4xx/5xx
- [x] Test: `.\mvnw.cmd clean test` xanh

**Dependencies:** Task 3

**Files:**
- `src/main/resources/static/index.html`

**Estimated scope:** Small (1 file)

---

## Task 5: Bottom sheet chi tiết POI + nút nghe thuyết minh

**Mô tả:** Bấm marker hoặc item danh sách mở sheet chi tiết với ảnh, tên, địa chỉ, chọn ngôn ngữ và nút mở player.

**Acceptance criteria:**
- [x] Bấm marker hoặc item POI mở sheet hiện ảnh, tên, địa chỉ
- [x] Sheet có chọn ngôn ngữ `vi`/`en` lấy từ `/api/languages`
- [x] Nút "Nghe thuyết minh" đi tới `player.html?locationId={id}&lang={code}`
- [x] Có nút đóng sheet, đóng bằng phím Escape cũng được
- [x] Sheet trượt được bằng cách kéo, không chặn thao tác cuộn trang

**Verification:**
- [x] Manual: bấm từng POI trong danh sách, sheet mở đúng POI, nút nghe dẫn tới đúng `locationId`
- [x] Manual: đổi ngôn ngữ trong sheet thì URL đổi theo
- [x] Test: `.\mvnw.cmd clean test` xanh

**Dependencies:** Task 4

**Files:**
- `src/main/resources/static/index.html`

**Estimated scope:** Small (1 file)

---

## Task 6: "Gần tôi" + khoảng cách Haversine

**Mô tả:** Nút "Gần tôi" lấy vị trí người dùng, thêm marker vị trí, sắp xếp danh sách POI theo khoảng cách thực.

**Acceptance criteria:**
- [x] Bấm "Gần tôi" hỏi quyền vị trí, được duyệt thì hiện marker vị trí hiện tại
- [x] Mỗi item trong danh sách hiện khoảng cách tính bằng Haversine, đơn vị km
- [x] Danh sách được sắp xếp tăng dần theo khoảng cách
- [x] Từ chối quyền vị trí thì hiện thông báo thân thiện, danh sách và bản đồ vẫn dùng được
- [x] Trình duyệt không có secure context (`window.isSecureContext === false`) thì ẩn nút "Gần tôi", không hiện lỗi

**Verification:**
- [x] Manual: DevTools override geolocation về TP.HCM, bấm "Gần tôi", danh sách sắp xếp lại, khác thứ tự ban đầu
- [x] Manual: chặn quyền vị trí trong DevTools, bấm "Gần tôi" thấy thông báo, trang vẫn dùng được
- [x] Test: `.\mvnw.cmd clean test` xanh

**Dependencies:** Task 5

**Files:**
- `src/main/resources/static/index.html`

**Estimated scope:** Small (1 file)

---

## Task 7: Danh sách tour + lọc POI theo tour

**Mô tả:** Thêm mục "Tuyến tham quan" lấy từ `/api/tours`, bấm một tour thì lọc danh sách POI theo lộ trình tour.

**Acceptance criteria:**
- [x] Mục "Tuyến tham quan" hiện tên, mô tả, thời lượng của mỗi tour từ `/api/tours`
- [x] Bấm một tour lọc danh sách POI còn lại POI thuộc `tour.locations`
- [x] Marker của POI thuộc tour được làm nổi bật, marker ngoài tour mờ đi
- [x] Có nút "Xem tất cả" khôi phục danh sách đầy đủ

**Verification:**
- [x] Manual: bấm "Tour Tuyến Xe Buýt Lịch Sử Quận 4", danh sách còn 3 trạm, bấm "Xem tất cả" về 6 POI
- [x] Test: `.\mvnw.cmd clean test` xanh

**Dependencies:** Task 6

**Files:**
- `src/main/resources/static/index.html`

**Estimated scope:** Small (1 file)

---

## Task 8: `player.html` đọc text thuyết minh thật từ API

**Mô tả:** Bỏ object `narrations` hardcode trong `player.html`, lấy text từ `/api/narrations` theo POI và ngôn ngữ đang chọn.

**Acceptance criteria:**
- [x] Xoá object `narrations` hardcode, text hiển thị lấy từ API
- [x] POI có bản thuyết minh hiện đúng text theo ngôn ngữ đang chọn
- [x] POI không có bản thuyết minh cho ngôn ngữ đó (ví dụ Trạm Khánh Hội với `en`) hiện thông báo "chưa có bản thuyết minh", không hiện text của ngôn ngữ khác
- [x] API lỗi thì hiện text mặc định sẵn có, không màn hình trắng, audio vẫn phát được
- [x] Đổi ngôn ngữ trong player cập nhật cả text lẫn nguồn audio

**Verification:**
- [x] Manual: `player.html?locationId=5&lang=en` hiện text tiếng Anh trong DB
- [x] Manual: `player.html?locationId=1&lang=en` hiện thông báo thiếu bản thuyết minh
- [x] Manual: tắt mạng rồi mở player, audio và text fallback vẫn chạy
- [x] Test: `.\mvnw.cmd clean test` xanh

**Dependencies:** Task 1

**Files:**
- `src/main/resources/static/player.html`

**Estimated scope:** Small (1 file)

---

## Task 9: Trang chủ vẫn dùng được khi mất mạng

**Mô tả:** Cache danh sách POI vào `localStorage` để trang chủ không chết khi mạng yếu hoặc mất.

**Acceptance criteria:**
- [x] Lần tải thành công cache danh sách POI vào `localStorage`
- [x] Lần sau fetch lỗi thì dùng bản cache và hiện banner "Đang offline, dữ liệu lưu cục bộ"
- [x] Chưa có cache và fetch lỗi thì hiện thông báo lỗi rõ ràng kèm link tải gói offline
- [x] Trang ghi rõ "Tile bản đồ OpenStreetMap cần Internet"

**Verification:**
- [x] Manual: DevTools chuyển sang Offline, tải lại trang, thấy POI từ cache và banner offline
- [x] Manual: xoá `localStorage`, Offline, tải lại trang, thấy thông báo lỗi thay vì trang trắng
- [x] Test: `.\mvnw.cmd clean test` xanh

**Dependencies:** Task 7

**Files:**
- `src/main/resources/static/index.html`

**Estimated scope:** Small (1 file)

---

## Task 10: Tài liệu, checklist và ADR

**Mô tả:** Cập nhật tài liệu cho khớp với trang chủ du khách mới và quyết định stack frontend.

**Acceptance criteria:**
- [x] README có mục "Trang chủ du khách": URL, các endpoint public, giới hạn tile OSM
- [x] README bảng API ghi rõ endpoint nào public, endpoint nào cần token
- [x] `TASK_CHECKLIST.md` mục P7-1 tick từng dòng kèm ghi chú, bảng trạng thái cập nhật
- [x] `docs/adr/0001-guest-ui-static-html-leaflet.md` ghi quyết định stack, lý do, hệ quả
- [x] Không còn câu nào trong README hoặc checklist mô tả `/api/locations` là cần token

**Verification:**
- [x] Đọc lại tài liệu, mọi link trong README còn dẫn tới file thật
- [x] `.\mvnw.cmd clean verify` xanh

**Dependencies:** Task 9

**Files:**
- `README.md`
- `TASK_CHECKLIST.md`
- `docs/adr/0001-guest-ui-static-html-leaflet.md` (mới)

**Estimated scope:** Small (3 files)

---

## Checkpoint: Sau Task 1-2 (API công khai)
- [x] `.\mvnw.cmd clean test` xanh
- [x] `GET /api/locations/pois` không token trả 200
- [x] `POST /api/languages` không token trả 401
- [x] `GET /api/narrations?locationId=5&languageCode=vi` trả text trong DB
- [x] Người review duyệt trước khi sang Phase 2

## Checkpoint: Sau Task 3-5 (luồng du khách chính)
- [x] Mở `/` trên DevTools mobile viewport: thấy bản đồ, danh sách POI, sheet chi tiết
- [x] Bấm POI phát đúng audio đúng ngôn ngữ
- [x] Console trình duyệt không lỗi
- [x] `.\mvnw.cmd clean test` xanh
- [x] Người review duyệt trước khi làm "Gần tôi" và tour

## Checkpoint: Sau Task 6-7 (gần tôi và tour)
- [x] "Gần tôi" sắp xếp lại danh sách theo khoảng cách
- [x] Từ chối quyền vị trí thì trang vẫn dùng được
- [x] Bấm tour lọc đúng POI, "Xem tất cả" khôi phục
- [x] `.\mvnw.cmd clean test` xanh

## Checkpoint: Sau Task 8-9 (player và offline)
- [x] Player hiện text thật theo ngôn ngữ, thiếu ngôn ngữ thì có thông báo
- [x] Trang chủ sống khi Offline nhờ cache `localStorage`
- [x] `.\mvnw.cmd clean test` xanh

## Checkpoint: Hoàn tất (sau Task 10)
- [x] Mọi tiêu chí chấp nhận ở trên đạt
- [x] `.\mvnw.cmd clean verify` xanh
- [x] `TASK_CHECKLIST.md` và README cập nhật
- [x] Nhánh `feature/guest-ui-map` có PR vào `develop`
