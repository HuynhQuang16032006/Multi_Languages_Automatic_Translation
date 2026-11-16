# Vòng 3 — React chính thức và quyết định nộp

## Kết luận: NOT_READY

`/` đã chuyển sang React; còn QR do admin React sinh ra trỏ `/player.html` legacy, TTS/DeepL thật chưa kiểm chứng, GPS cần xác nhận và E2E chưa đầy đủ. Không dùng kết quả legacy để chứng minh React.

## Kiến trúc và route

| Trước | Sau |
|---|---|
| `/` phục vụ `src/main/resources/static/index.html` (HTML/vanilla JS/Leaflet); React tại `/next` | `/` HTTP 302 đến `/next/explore` React; `/?venue=1` chuyển `/next/places/1?source=qr&venue=1` |
| `/next/admin` là admin React song song `dashboard.html` | `/next/admin` và `/next/admin/{page}` là admin React chính thức; `/admin` HTTP 401 do SecurityConfig hiện hành, không đổi security |
| QR React admin tạo `/player.html?qr=...` | **Chưa chuyển:** QR mới từ admin vẫn mở legacy; QR cũ vẫn hoạt động, cần xử lý tương thích trước khi coi React-only hoàn tất |

Vite base `/next/`; Spring `FrontendController` forward route SPA về `/next/index.html`, giữ refresh. Legacy source `/src/main/resources/static/{index,player,dashboard,demo}.html` không sửa/xóa; URL cũ tiếp tục HTTP 200. Backend, APIs, audio `uploads/audio`, DB và security dùng chung, không loại bỏ.

## Bản đồ chức năng

| Legacy | React tương đương | Gap |
|---|---|---|
| Bản đồ/POI/chi tiết | `/next/explore`, `/next/map`, `/next/places/:id` | GPS ba tầng và thiết bị chưa chứng minh; tìm không dấu `Dinh` không match `Dinh Độc Lập` |
| Player/audio/ZIP | `/next/player/:id` (player đặt ngoài Routes), stream/ZIP API | Phát thật; ZIP UI vòng 2, chưa kiểm tra lại sau chuyển route |
| QR player/venue | `/next/qr`, redirect root venue | Mã mới admin vẫn trỏ legacy `/player.html` |
| Dashboard/login | `/next/admin`, charts | Dashboard có thật; heatmap/tỷ lệ đầy đủ chưa xác minh |
| Quản lý địa điểm/narration | `/next/admin/places`, `/next/admin/audio` | Browser tạo/sửa; xóa/confirm chưa xác minh |
| Định vị/geofence | `/next/map` | Chỉ gợi ý; PRD yêu cầu tự phát, kế hoạch chấp nhận gợi ý — NEEDS_TEACHER_CONFIRMATION |

## Thay đổi và subagent

- `FrontendController.java`, `FrontendRoutingTest.java`, `GuestHomePageTest.java`: redirect root, tương thích `venue`, kiểm thử route và bảo vệ API. `ReactPolish3`: bỏ banner/link legacy trong `frontend/src/main.jsx`, `frontend/src/admin.jsx`; truyền nguồn QR venue đến player. `RouteImplement3` sở hữu controller/test.
- `AuthService.java`, `AuthServiceTest.java`: `BackendRound3` ngăn bootstrap thay username/hash của admin đã tồn tại khi đổi profile; tài khoản chỉ tạo khi DB trống. Đây là thay đổi contract vận hành: thay biến môi trường không đổi tài khoản đã tồn tại.
- `RouteAudit3`, `ReactAudit3`, `DocRound3`: khảo sát legacy/React, API, PRD; nhận định player React trắng bị bác bỏ bởi browser thực vì AudioPlayer render ngoài Routes.
- `BrowserRound3`: browser E2E từng phần; `RouteSmoke3` chưa trả kết quả. `QrReact3` bị hủy vì không tiến triển; QR tạo mới chưa được chuyển. Không commit/push/deploy.
- README cập nhật route. Các thay đổi vòng trước (V5, DTO validation, QR state) được giữ nguyên.

## Verification

- MySQL 8.0 tạm riêng, cổng 33316, hai schema test trống: Flyway V1–V5 thành công, không truy cập DB ứng dụng. `mvnw.cmd package`: **188 tests, 0 failures/errors/skipped, BUILD SUCCESS**. Lần đầu trên DB test cũ có 24 failures (23 do credential fixture và 1 contract root); DB test cũ không bị đổi credential để ép test qua. Lần kế trên DB sạch lỗi assertion URL tuyệt đối, đã sửa rồi chạy thành công.
- `npm test`: 51/51 pass trước sửa banner/QR venue; Maven package sau sửa đã build Vite thành công, nhưng chưa chạy lại frontend suite sau mọi sửa. Vite có cảnh báo React Router `use client`.
- JAR hiện hành trên `127.0.0.1:18081`, DB tạm 33316: health UP. `/` 302 `/next/explore`; `/next/admin`, `/next/admin/places` 200; `/api/locations/pois` 200, dashboard API anonymous 401; `/admin` 401. Browser mobile 375×812 không tràn, root hiển thị React; `/?venue=1` hiện detail React.
- Browser trên React: QR seed nhập tay HTTP 200; chọn `en`/`vi` narration HTTP 200, quan sát nội dung tiếng Anh; player vi stream 206, thời lượng 18.432s, tiến độ 6.247s; heartbeat HTTP 200, dashboard có phiên. Admin login 200, tạo POI/narration 201, sửa 200, form required chặn input trống. Không xác minh xóa, logout, payload JOIN/PING/LEAVE, refresh/dedup hoặc camera thật.
- Trên bản này chưa kiểm tra ZIP từ browser, chưa build Compose image mới. Đường dẫn legacy player HTTP 200 chỉ là bằng chứng bảo tồn, không chứng minh React.
- `graphify update .` thành công, thiếu `tree_sitter_sql` nên SQL không trích vào graph.

## P0/P1 và phụ thuộc

1. QR admin mới đang phát URL legacy, không đạt React-only. Chuyển QR sang React mà vẫn giữ truy cập các mã đã in; phải giữ semantics mã bị vô hiệu khi admin đổi mã.
2. Hoàn tất browser E2E CRUD xóa, logout, QR JOIN/PING/LEAVE/dedup, ZIP và đầy đủ ngôn ngữ. Không dùng test đơn vị làm thay thế.
3. `BLOCKED_EXTERNAL`: ElevenLabs/DeepL thật chưa gọi vì chưa duyệt chi phí; HTTP client chưa cấu hình connect/read timeout rõ ràng. Test mock không chứng minh upstream.
4. `NEEDS_TEACHER_CONFIRMATION`: geofence tự phát hay gợi ý. Camera/GPS thực địa và HTTPS điện thoại `UNVERIFIED_DEVICE`.
5. Trước V5 trên DB ứng dụng: backup, đối chiếu duplicates và nullability trên bản sao; không tự xóa dữ liệu. Chưa kiểm tra bản sao DB ứng dụng.

Không thay credentials ứng dụng, không xóa volume, không dừng Compose hiện hữu. MySQL tạm và JAR test được dừng sau nghiệm thu; tài khoản/bootstrap chỉ nằm trong DB tạm.
