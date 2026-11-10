# Audio Tour — React frontend

Giao diện mới chạy song song HTML cũ, chưa thay trang mặc định. Lát cắt hiện tại gồm Home/Explore và API client; route chi tiết dẫn tới trình phát HTML hiện có bằng ID địa điểm đã chọn.

## Chạy local

Khởi động MySQL và backend theo cấu hình dự án. Backend cần phục vụ `http://127.0.0.1:8080`. Từ thư mục `frontend`:

```powershell
npm ci
npm run dev
```

Mở `http://127.0.0.1:5173/next/`. Vite proxy `/api`, `/index.html` và `/player.html` tới backend. Không dùng dữ liệu mẫu khi backend lỗi. Không có backend thì giao diện hiển thị lỗi và nút thử lại.

## Kiểm tra

```powershell
npm test
npm run build
```

Test frontend kiểm tra API client và quy tắc ảnh bằng dữ liệu kiểm soát trong test, không phải tích hợp database. Production build ra `dist/`. Chạy `./mvnw package` (Windows: `.\mvnw.cmd package`) từ root: Maven tải Node 24.12.0, chạy `npm ci`, `npm run build`, xóa asset `/next` đã copy trước đó rồi đóng gói frontend mới vào JAR. Không cần build frontend thủ công trước Maven; lỗi npm/Vite làm Maven thất bại. Trên Windows, dừng Vite dev server trước `mvnw package` vì `npm ci` không thể ghi đè file Rollup đang được Vite mở. Dừng JAR đang chạy từ `target/` trước khi đóng gói lại cùng đường dẫn. CI và Docker dùng cùng quy trình Maven. Spring Boot phục vụ `/next/`, `/next/explore`, `/next/places/{id}`; API admin vẫn yêu cầu JWT.

## Điểm dừng hiện tại

- Frontend test: 4/4 đạt; Vite build đạt, còn hai cảnh báo `use client` từ React Router.
- Sau khi MySQL chạy: `mvnw package` đạt, 142 test backend đạt. Test routing đã thất bại với 401 trước thay đổi, sau đó đạt và xác nhận dashboard API vẫn trả 401 khi thiếu token. Kiểm tra JAR có HTML/CSS/JS tại `BOOT-INF/classes/static/next`.
- Spring Boot tại cổng 8080 trả 200 cho route Explore và API trả 6 POI thật. Chrome ở 375px/1440px đã tải Home và Explore, không tràn ngang. Search trả trạng thái rỗng, sort A–Z và card mở đúng ID; refresh route Explore/chi tiết chạy trong JAR ở cổng 8081. Ảnh trong dữ liệu hiện có có ảnh không khớp tên địa điểm; chưa nghiệm thu hình ảnh.
- Sau thay đổi Maven, `mvnw package` đạt 142 test (0 lỗi/skip) và JAR chỉ chứa asset JS/CSS mới trong `BOOT-INF/classes/static/next`, kể cả khi chạy `package` nhiều lần không `clean`. Chrome trên JAR xác nhận Home/Explore 375px/1440px không tràn ngang, Back/Forward, Tab vào skip link rồi qua header, search/select/card và Enter mở đúng card; focus vòng 3px trên skip link và ô tìm kiếm. Sau đổi route, main nhận focus; khi nhập search, ô nhập giữ focus. Không có lỗi console/network khi ảnh chưa xác minh được thay bằng placeholder. Đã xem ảnh chụp bốn trang sau chỉnh thẩm mỹ: card dùng họa tiết trung tính và nhãn ảnh chưa xác minh thay vì lặp lại tên POI; chữ nav/địa chỉ lớn hơn. Nguồn ảnh Unsplash trong API có 1 URL trả 404 gây `ERR_BLOCKED_BY_ORB`; 5 URL trả JPEG nhưng chưa xác minh đúng địa điểm và quyền dùng. UI `/next` không hiển thị nhóm URL này; không sửa dữ liệu gốc.
- Đã đọc cấu hình GitHub Actions: job `build-and-test` cài Node, chạy test frontend, `./mvnw clean verify` chạy build frontend và backend; job Docker phụ thuộc job này và chỉ chạy khi push. Chưa chạy workflow trên GitHub hoặc Docker build (máy không có lệnh `docker`), nên chưa xác nhận chúng đạt. Chưa kiểm tra GPS/camera trên thiết bị và audit accessibility đầy đủ. Nearby, Map, geofence, Player React và Admin chưa triển khai. HTML cũ chưa bị sửa hoặc xóa.

## Giai đoạn C, lát cắt Nearby (đang triển khai)

Home có mục Địa điểm gần bạn với tối đa 5 POI từ API; Explore hỗ trợ tìm kiếm kết hợp sắp xếp Gần tôi và bỏ sắp xếp để trở về thứ tự trước. Chỉ xin vị trí khi bấm nút, dùng Haversine tính đường chim bay; không dùng IP hay tọa độ mặc định. Vị trí chỉ giữ trong bộ nhớ trang, có hạn 5 phút và cảnh báo độ chính xác thấp trên 100 m. Không gửi vị trí qua chức năng Nearby mới; trình phát HTML cũ có thể gửi vị trí qua heartbeat khi dùng tính năng của nó. Khi từ chối quyền, người dùng vẫn xem danh sách thường; cần đổi quyền trong cài đặt trình duyệt rồi thử lại sau khi tải lại trang.

Test frontend cho khoảng cách, tọa độ không hợp lệ, số lượng POI ít, trạng thái GPS. Tại thời điểm viết phần này, MySQL chưa chạy nên backend test và API thật chưa thể kiểm tra lại; xem kết quả mới nhất ở phần dưới. Không đánh dấu điểm kiểm C hoặc A/B hoàn tất. Map, Player, QR, About, geofence và offline React vẫn chưa triển khai.

## Kiểm tra sau khi MySQL khởi động, lát cắt C3

`mvnw.cmd test -q`: 142 test, không lỗi/skip. Backend ở 8080 trả POI và chi tiết thật; Home/Explore trên Vite hiển thị dữ liệu, tìm kiếm giữ từ khóa khi bỏ Gần tôi. Chrome kiểm tra từ chối quyền GPS: hiển thị hướng dẫn, vẫn xem danh sách; chưa kiểm tra nhánh cấp quyền. Đã xem ảnh Home và chi tiết ở 375px/1440px; script CDP xác nhận Home và Detail không tràn ngang tại 375/768/1024/1440px. Trình duyệt Review không chụp được ảnh vì cửa sổ không hiển thị, dùng Chrome headless riêng. C3 mở trực tiếp `/next/places/4`, lấy địa điểm/khu vực con/thuyết minh tiếng Việt từ API, link player HTML cũ theo ID và bản đồ HTML cũ qua `?venue=`; `/next/places/999999` hiển thị không tìm thấy. Chưa nghiệm thu chi tiết bằng bàn phím/screen reader hoặc thiết bị HTTPS. C4–C7 vẫn chưa làm, gate A/B và C vẫn mở.

Chi tiết có danh sách ngôn ngữ từ `/api/languages`; khi chọn ngôn ngữ khác, giao diện đọc lại văn bản từ API và truyền `lang` sang trình phát HTML cũ. Thiếu bản dịch hiển thị trạng thái thiếu thuyết minh thay vì thay bằng văn bản minh họa. Link bản đồ dẫn sang `index.html?venue=<id>`; HTML cũ có thể tự xin GPS khi vào bằng liên kết venue trên kết nối an toàn. Chưa chuyển hành vi GPS này sang React. Test frontend 15/15 và Vite build đạt; còn hai warning `use client` của React Router. Cần xác minh nhánh cấp quyền GPS và luồng chọn ngôn ngữ trên thiết bị trước khi nghiệm thu C3.

Sau lượt sửa C3: `mvnw.cmd package -q` đạt 142/142 test; JAR chứa đúng asset mới. Mở trực tiếp `/next/places/4` trên JAR cổng 8081: tải API địa điểm, khu vực con, ngôn ngữ và văn bản (đều HTTP 200), không có cảnh báo/lỗi console. `mvnw.cmd compile -q` đạt. Chưa chạy Docker hoặc GitHub Actions.

## C4–C7: đang triển khai, chưa nghiệm thu

- Player React có phát/pause, tua, ngôn ngữ, transcript, tải ZIP, mini-player và heartbeat. Trên trình duyệt trước lần khởi động lại đã phát/pause audio thật và xác nhận audio tiếp tục khi sang Explore. Cần kiểm tra thêm phiên heartbeat khi tua, đổi ngôn ngữ, đóng trang và xử lý lỗi.
- Map dùng Leaflet và tile Esri như HTML cũ, danh sách/marker từ POI thật; có tìm kiếm, chọn địa điểm và vùng chính xác GPS. Chưa kiểm tra giao diện cuối trên đủ viewport hoặc khi mất mạng.
- Geofence có test vùng chồng nhau, cooldown, hysteresis và vị trí cũ/sai số cao. Bản hiện tại chỉ đề xuất địa điểm khi vào vùng, **chưa tự phát**, nên C6 chưa đạt yêu cầu thuyết minh tự động. Ngưỡng 30 m, 15 giây, hysteresis 15 m và cooldown 60 giây là tạm thời, cần kiểm tra thực địa.
- QR có nhập mã và quét bằng BarcodeDetector khi trình duyệt hỗ trợ; không điều hướng URL khác origin. Camera và QR trên điện thoại chưa kiểm tra. About và cache POI có nhãn thời điểm; audio/tile chưa hỗ trợ offline trong web.
- Sau khi môi trường khởi động lại, dịch vụ MySQL dừng; tài khoản hiện tại không có quyền bật `MySQL80`. `npm test`: 29/29; `npm run build` và `mvnw.cmd compile -q` đạt. Chưa chạy lại backend suite, JAR hoặc kiểm tra browser trên bản cuối. Giữ nguyên HTML cũ và không đánh dấu gate C hoàn tất.

### Kiểm tra sau khi MySQL mở lại

- `npm test`: 29/29; `mvnw.cmd package -q`: 142 test backend, không lỗi hoặc bỏ qua. JAR mới chạy trên cổng 8081.
- Map React tải Leaflet, sáu POI và sáu marker thật. Chưa xác nhận tương tác marker, responsive và GPS thực địa.
- Nhập `QR-KHANH-HOI-01` trên trang QR mở đúng `/next/places/1`; camera thật chưa kiểm tra.
- Player phát audio Dinh Độc Lập và tiếp tục phát khi chuyển sang Explore, mini-player xuất hiện. Chưa xác nhận đầy đủ thứ tự heartbeat, tua, đổi ngôn ngữ, tải ZIP hoặc đóng trang.
- C6 vẫn chỉ gợi ý điểm mới, chưa tự phát. Không đánh dấu C4–C7 hoặc điểm kiểm C hoàn tất.
