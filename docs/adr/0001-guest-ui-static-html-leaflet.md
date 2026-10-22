# ADR 0001: Giao diện du khách dùng HTML tĩnh + Leaflet, không dùng framework SPA

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted (nguồn tile thay bởi ADR 0003) |
| Ngày | 2026-10-21 |
| Nhánh | `feature/guest-ui-map` |

## Bối cảnh

PRD xác định mục tiêu "không rào cản": du khách phải tiếp cận được nội dung ngay, không bắt buộc đăng ký tài khoản. `TASK_CHECKLIST.md` mục P5-1 đã chốt không dùng tài khoản người dùng, chỉ admin đăng nhập để quản trị. PRD mục FR-1 (tra cứu POI, tra cứu theo mã QR) và FR-2 (kích hoạt bằng QR) đòi hỏi một màn hình để du khách xem điểm tham quan trên bản đồ và nghe thuyết minh.

Ba ràng buộc kỹ thuật của repo:

- Backend Java 21 + Spring Boot, 3 tầng, MySQL. Không có toolchain frontend nào: không npm, không bundler, không test runner cho UI.
- Quy trình build và deploy chỉ gói JAR (Maven → Dockerfile multi-stage → JRE slim), CI chạy trên GitHub Actions. Không có pipeline cho tài nguyên frontend.
- Cần demo và thuyết trình trên laptop: chỉ cần chạy app rồi mở trình duyệt, không cài thêm môi trường.

Từ đó đặt ra câu hỏi: xây giao diện du khách bằng công cụ nào để vừa đáp ứng yêu cầu trải nghiệm du khách, vừa không phá vỡ quy trình build hiện có.

## Quyết định

Giao diện du khách là **HTML tĩnh + vanilla JavaScript**, đặt trong `src/main/resources/static/`, **không dùng framework SPA, không có bước build**.

Cụ thể:

1. `src/main/resources/static/index.html` là trang chủ du khách: bản đồ Leaflet, danh sách POI, bottom sheet chi tiết, nút "Gần tôi" (định vị qua `navigator.geolocation`, sắp xếp theo khoảng cách Haversine) và danh sách tour. CSS và JavaScript viết tay nằm ngay trong file, không tách file `.js`/`.css` riêng, không bundler.
2. Bản đồ dùng **Leaflet 1.9.4 tải từ CDN unpkg** (CSS + JS) với **một nguồn tile duy nhất: OpenStreetMap** (`tile.openstreetmap.org`), kèm attribution OSM. Không cần API key, không có chi phí. Trang chủ có nút đổi lớp (`L.control.layers`) giữa lớp **đường phố** (tile OSM nguyên bản) và lớp **tối** — lớp tối dùng *chung* tile OSM rồi đảo màu bằng CSS filter (`invert` + `hue-rotate`), nên không phụ thuộc nhà cung cấp bản đồ thứ hai. Lý do bỏ CartoDB: chính sách hiện tại của CARTO yêu cầu API key (miễn phí, cần email) cho mọi lớp basemap, còn URL không key có thể bị throttle hoặc chặn.
3. Trang chủ chỉ đọc, gọi các API GET đã mở public trong `SecurityConfig`: `GET /api/locations/pois`, `GET /api/languages`, `GET /api/narrations`, `GET /api/tours`. Mọi API ghi vẫn yêu cầu token admin; du khách không cần đăng nhập.
4. `src/main/resources/static/player.html` lấy kịch bản thuyết minh thật từ `GET /api/narrations?locationId=&languageCode=`, phát âm thanh qua `GET /api/packages/stream`, và ghi nhận phiên nghe ẩn danh qua `POST /api/analytics/heartbeat` với `deviceId` sinh ở client và lưu trong `localStorage`.
5. Các file tĩnh được Spring Boot serve trực tiếp. Thay đổi giao diện = sửa file trong `src/main/resources/static/`, không sinh ra artifact riêng.

## Các lựa chọn đã cân nhắc và lý do loại

| Lựa chọn | Lý do loại |
| --- | --- |
| React/Vue SPA trong một repo riêng | Thêm toolchain frontend, pipeline build (lint, test, bundle) và pipeline deploy riêng, tách repo thành hai sản phẩm; ở giai đoạn hiện tại không đổi giá trị bài toán cho du khách ẩn danh. |
| Thymeleaf server-side render trong backend | Thêm template engine vào JAR, và mỗi lần mở bản đồ phải round-trip qua server cho phần HTML động, trái với bản chất tĩnh của trang bản đồ. |
| Google Maps / Mapbox (thư viện bản đồ thương mại) | Cần API key và có chi phí, trái yêu cầu "miễn phí" của đồ án; Leaflet + tile OSM đã đáp ứng đủ nhu cầu marker, popup và định vị. |
| React Native / Flutter cho mobile | Nằm ngoài phạm vi nhóm backend; PRD đã để tích hợp mobile app ở giai đoạn 2 của roadmap. |

## Hệ quả

Tích cực:

- Không thêm toolchain và không thêm bước build: `.\mvnw.cmd package` vẫn là lệnh duy nhất tạo ra artifact triển khai, CI/CD không phải đổi.
- Triển khai đúng một artifact (JAR) chứa cả API lẫn giao diện, không có trạng thái "đã deploy UI hay chưa".
- Demo nhanh trên laptop: chạy app, mở `http://localhost:8080/`, không cài thêm công cụ.
- Rủi ro bảo mật không tăng: trang chủ chỉ đọc dữ liệu công khai, mọi thao tác ghi vẫn nằm sau JWT của admin.
- Trang nhẹ, tải nhanh trên điện thoại, không tốn thời gian render phía server.

Tiêu cực:

- Không có component tái sử dụng. CSS và JavaScript viết tay lặp lại giữa các trang trong `static/`; thêm một trang mới nghĩa là thêm một file mới.
- Phụ thuộc CDN ở nơi trình bày (unpkg cho Leaflet, Google Fonts, `tile.openstreetmap.org` cho tile). Mất mạng thì bản đồ và font không hiển thị; danh sách POI vẫn dùng được vì gọi API của chính máy chủ.
- Sửa giao diện không có kiểm tra kiểu dữ liệu hay test component tự động, phải kiểm thử thủ công trên trình duyệt.
- Trang không hoạt động offline: cần mạng để tải tile và gọi API. Gói nội dung offline hiện do API đóng gói, không do trang web xử lý.
- Khi mở rộng UI lớn, chi phí viết tay tăng nhanh và sẽ phải cân nhắc lại framework.

## Khi nào xem lại quyết định

Xem lại ADR này nếu xuất hiện một trong các điều kiện sau:

- Giao diện web cần đăng nhập hoặc nhiều vai trò (quản trị, nhân viên, đối tác): phải quản lý phiên và bảo vệ route ở phía client, đòi hỏi công cụ có router và state management.
- Số trang du khách vượt quá 3 và bắt đầu chia sẻ component chung (header, bộ lọc, card POI, mẫu xử lý lỗi).
- Cần trải nghiệm offline đầy đủ trên web: tile bản đồ tải trước, service worker, PWA. Cách hiện tại không đáp ứng được.
- Cần tích hợp frontend vào CI/CD: unit test UI, lint, kiểm tra bundle, deploy riêng. Khi đó phải có repo và pipeline riêng cho frontend.
- Backend được bổ sung template engine, hoặc nhóm đã có toolchain frontend dùng chung.

## Tham chiếu

- `docs/PRD.md`: mục tiêu zero-friction và bảo vệ người dùng ẩn danh; FR-1.3 (`GET /api/locations/pois`), FR-1.4 (tra cứu theo QR), FR-2.2 (kích hoạt bằng QR), FR-4.1 (ghi nhận lượt nghe ẩn danh).
- `TASK_CHECKLIST.md`: P5-1 (du khách không đăng ký, API đọc public, API gì yêu cầu token admin), P6-1 (chốt stack giao diện, ghi quyết định vào `docs/adr/`), P7-1 (trang chủ du khách: bản đồ + danh sách POI, marker, nút "gần tôi", API public).
- `docs/agents/conventions.md`: quy ước đặt ADR trong `docs/adr/`.
- Mã nguồn liên quan: `src/main/resources/static/index.html`, `src/main/resources/static/player.html`, `src/main/java/vn/edu/sgu/audiotour/config/SecurityConfig.java`.
