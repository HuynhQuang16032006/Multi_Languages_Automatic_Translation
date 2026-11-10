# ADR 0007: Dựng giao diện React song song với HTML hiện có

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted; thay thế phần lựa chọn giao diện trong ADR 0001 khi hoàn tất chuyển đổi |
| Ngày | 2026-11-08 |

## Bối cảnh

ADR 0001 chọn HTML tĩnh vì lúc đó chưa có toolchain frontend và số trang ít. Kế hoạch hiện tại gồm nhiều màn hình Tourist và Admin, chia sẻ điều hướng, card địa điểm, bộ lọc và trạng thái tải/lỗi. Viết tiếp các màn hình này trong các file HTML độc lập sẽ lặp lại giao diện và logic. Chức năng GPS, QR, audio và các URL HTML đang hoạt động vẫn cần duy trì trong khi xây bản mới.

## Quyết định

- Dùng React, React Router và Vite trong `frontend/`; giữ `src/main/resources/static/` và các URL cũ. Đặt bản mới tại `/next/`, chưa thay trang chủ `/`.
- Spring Boot chuyển các route giao diện `/next/` về cùng `index.html`. Chỉ cho phép GET các route này; API ghi của admin tiếp tục yêu cầu JWT. Dữ liệu địa điểm đọc từ API hiện có, không lấy địa điểm minh họa của prototype làm dữ liệu thật.
- Maven chạy `npm ci` và `npm run build` trước khi copy `frontend/dist` vào `static/next` của JAR; dùng Node 24.12.0 do `frontend-maven-plugin` tải về. Xóa bản copy `target/classes/static/next` trước khi copy để file JS/CSS cũ không sót lại trong JAR khi chạy `package` nhiều lần. CI chạy test frontend riêng rồi Maven build cả hai phần; Docker dùng cùng quy trình Maven.

## Hệ quả và điều kiện chuyển đổi

Một JAR vẫn phục vụ cả API và giao diện. `mvnw package` cần mạng để cài Node/npm và dependency trong lần build đầu; nếu build frontend lỗi, Maven dừng thay vì đóng gói dist cũ. Trên Windows, cần tắt Vite dev server dùng chung `frontend/node_modules` trước khi Maven chạy `npm ci`; JAR đang chạy cũng phải dừng trước khi ghi đè cùng đường dẫn. Kiểm tra route và static asset của JAR sau mỗi thay đổi tích hợp.

Giữ đường dẫn và chức năng HTML cũ cho đến khi từng màn hình React thay thế được kiểm thử trên thiết bị phù hợp. Route `/next/places/:id` hiện chỉ chuyển tiếp sang player HTML; nó chưa phải trang chi tiết React. Ảnh từ API cần được đối chiếu địa điểm và quyền sử dụng trước khi nghiệm thu hình ảnh. Phần Leaflet và chính sách tile trong ADR 0001/0003 vẫn áp dụng cho HTML cũ, chưa quyết định triển khai bản đồ React.
