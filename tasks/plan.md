# Kế hoạch: Trang chủ du khách + bản đồ (P7-1)

## Tổng quan

Trang `index.html` cho du khách: bản đồ Leaflet với marker POI, danh sách POI, nút "Gần tôi", bottom sheet chi tiết và danh sách tour. Mọi dữ liệu lấy từ API hiện có, không cần endpoint bản đồ riêng. Song song, `player.html` chuyển từ text thuyết minh hardcode sang đọc từ bảng `narrations`.

Repo quy ước lưu plan ở `docs/plans/`; tài liệu này nằm ở `tasks/` theo yêu cầu phiên làm việc. Chi tiết từng task nằm ở `tasks/todo.md`.

## Quyết định kiến trúc

| Quyết định | Lý do |
|---|---|
| HTML tĩnh + vanilla JS trong `src/main/resources/static/` | Repo không có build step. Ba trang cũ (`player.html`, `dashboard.html`, `demo.html`) đều là HTML tĩnh. Thêm React/Vue sẽ kéo theo toolchain mới cho một đồ án. |
| Leaflet 1.9 từ unpkg, tile OpenStreetMap | Không cần API key, không tốn tiền. Repo đã dùng CDN (Google Fonts, Unsplash) nên nhất quán. |
| `/api/locations/pois` làm nguồn marker | Endpoint này đã trả đủ `latitude`, `longitude`, `imageUrl`, `qrCode`, `priority` cho 6 POI. Không viết endpoint mới. |
| Mở `GET` đọc cho du khách, giữ `POST/PUT/DELETE` khoá admin | `SecurityConfig` thêm matcher `HttpMethod.GET`. Chỉ đọc dữ liệu công khai, không đụng API quản trị. |
| `GET /api/narrations` trả DTO, không trả entity | `open-in-view: false`. `Location.subLocations` lazy, trả entity `Narration` lồng `Location` sẽ gây `LazyInitializationException` khi serialize JSON. |
| Test Java cho phần API, verify browser cho phần UI | Repo không có test runner JS. Thêm Playwright là một task riêng, không gộp vào plan này. |

## Đồ thị phụ thuộc

```
SecurityConfig (permit GET đọc)  ─────┐
GET /api/narrations (T1)  ────────────┤
GET /api/locations/pois (có sẵn) ─────┼──> index.html (T3..T7, T9)
GET /api/locations (có sẵn) ──────────┤
GET /api/tours (có sẵn) ──────────────┤
GET /api/languages (có sẵn) ──────────┘
Leaflet CDN + tile OSM ────────────────┘

GET /api/narrations (T1) ────────────────> player.html (T8)
```

Thứ tự thực thi: T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8 → T9 → T10. T1 và T2 cùng sửa `SecurityConfig` nên chạy tuần tự, mỗi task một commit.

## Điều kiện bắt đầu

- Có MySQL chạy ở `localhost:3306`, database `audiotour_db` đã có seed (`DataInitializer`).
- Working tree đang có `TASK_CHECKLIST.md` sửa chưa commit. Commit hoặc stash trước khi tạo nhánh.
- Tạo nhánh: `git checkout -b feature/guest-ui-map` từ `main`.

## Danh sách task

Chi tiết đầy đủ (tiêu chí chấp nhận, bước kiểm chứng, file, kích thước) nằm ở `tasks/todo.md`.

### Phase 1: API công khai cho du khách
- [ ] T1: `GET /api/narrations` trả text thuyết minh thật, public
- [ ] T2: Mở public các `GET` đọc (locations, pois, tours, languages)

**Checkpoint CP1:** `.\mvnw.cmd clean test` xanh, `GET /api/locations` không token trả 200, `POST /api/languages` không token trả 401.

### Phase 2: Trang chủ du khách
- [ ] T3: `index.html` + entry `/` + danh sách POI nối tới player
- [ ] T4: Bản đồ Leaflet + marker POI
- [ ] T5: Bottom sheet chi tiết POI + nút nghe thuyết minh

**Checkpoint CP2:** mở `/` trên viewport mobile thật, bấm POI phát đúng audio đúng ngôn ngữ.

- [ ] T6: "Gần tôi" + khoảng cách Haversine
- [ ] T7: Danh sách tour + lọc POI theo tour

**Checkpoint CP3:** "Gần tôi" sắp xếp lại danh sách, bấm tour lọc đúng POI, console trình duyệt không lỗi.

### Phase 3: Player và chế độ suy giảm
- [ ] T8: `player.html` đọc text thuyết minh thật từ API
- [ ] T9: Trang chủ vẫn dùng được khi mất mạng

**Checkpoint CP4:** player hiện đúng text theo ngôn ngữ, trang chủ sống khi offline.

### Phase 4: Tài liệu
- [ ] T10: README, `TASK_CHECKLIST.md` (P7-1), ADR stack frontend

**Checkpoint CP5:** toàn bộ tiêu chí chấp nhận đạt, `.\mvnw.cmd clean verify` xanh, review với người hướng dẫn.

## Rủi ro

| Rủi ro | Mức | Giảm thiểu |
|---|---|---|
| CDN unpkg bị chặn ở nơi trình bày, bản đồ trắng | Trung bình | Vendor Leaflet vào `static/vendor/leaflet/` (leaflet.js, leaflet.css, 2 file marker) khi cần. Để sau task T4. |
| `open-in-view: false` + `Location.subLocations` lazy gây 500 khi serialize `Narration` | Cao nếu trả entity | T1 trả DTO `NarrationResponse`, không lồng entity `Location`. |
| Mở `GET` công khai lộ dữ liệu POI | Thấp | POI là dữ liệu công khai. `POST/PUT/DELETE` giữ khoá admin, có test chứng minh. |
| 3 trạm xe buýt chỉ có narration tiếng Việt | Trung bình | Sheet chi tiết ẩn nút nghe khi thiếu ngôn ngữ. Player hiện thông báo rõ ràng. |
| `navigator.geolocation` chỉ chạy trong secure context (HTTPS hoặc localhost) | Trung bình | Trên LAN `http://192.168.x.x` bị chặn. Nút "Gần tôi" ẩn luôn khi `window.isSecureContext` false, danh sách vẫn dùng được. |
| Nhiều task cùng sửa `SecurityConfig` | Thấp | Chạy tuần tự, một task một commit, mỗi task có test riêng. |
| 6 task sau đều sửa `index.html` | Trung bình | T4 đến T7 tách bạch theo vùng DOM: `<div id="map">`, `<ul id="poiList">`, `<div id="sheet">`, `<section id="tours">`. T9 tách phần cache vào function riêng. |

## Câu hỏi còn mở

- Trình bày offline thì cần vendor Leaflet. Chốt khi nào chuyển từ CDN sang bản local.
- Có đưa số "du khách đang online" lên trang chủ không. Mặc định: không, để sau P6-1.
- Có chạy CI check giao diện bằng Playwright không. Mặc định: không, tách task riêng nếu team yêu cầu.

## Ngoài phạm vi

- P6-1 dashboard CMS và mở rộng API phân tích. Không đụng trong plan này.
- Tile bản đồ offline, Service Worker, PWA.
- Ứng dụng mobile Flutter/React Native.
- Tài khoản người dùng. P5-1 đã chốt du khách ẩn danh.
