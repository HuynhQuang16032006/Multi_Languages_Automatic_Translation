# Đặc tả: Đăng nhập admin cho dashboard

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Chưa triển khai |
| Ngày | 2026-11-07 |
| Liên quan | SecurityConfig, AuthController, dashboard.html |

## Vấn đề

`/dashboard.html` và `/api/analytics/dashboard` yêu cầu token admin, nhưng trang dashboard không có luồng đăng nhập. Trình duyệt gọi API không kèm token nên nhận 401 và không hiển thị dữ liệu.

## Kết quả mong muốn

Admin mở dashboard, đăng nhập bằng tài khoản admin, sau đó xem dashboard và gọi API analytics với token. Đăng xuất xoá token và quay về màn hình đăng nhập.

## Phạm vi

Trong phạm vi:
- Màn hình đăng nhập trên dashboard (username/password).
- Lưu token ở `sessionStorage` (mất khi đóng tab).
- Gửi `Authorization: Bearer <token>` cho `/api/analytics/dashboard` và các API admin khác.
- Đăng xuất.
- Xử lý 401 (token hết hạn/sai) → quay lại màn hình đăng nhập.

Ngoài phạm vi:
- Quản lý user/role nhiều cấp.
- Refresh token.
- Bảo vệ trang chủ du khách (vẫn public).

## Luồng

1. Admin mở `/dashboard.html`.
2. Nếu chưa có token trong `sessionStorage`, hiển thị form đăng nhập.
3. Admin nhập username/password, dashboard gọi `POST /api/auth/login`.
4. Thành công: lưu `token`, ẩn form, gọi `/api/analytics/dashboard` với header Bearer, render dữ liệu.
5. Thất bại 401: hiện thông báo "Sai thông tin đăng nhập".
6. Khi API trả 401 sau đó: xoá token, quay lại form đăng nhập.
7. Admin bấm "Đăng xuất": xoá token, quay lại form.

## API

### `POST /api/auth/login` (public)

Request:
```json
{ "username": "admin", "password": "admin123" }
```

Thành công 200:
```json
{ "token": "...", "tokenType": "Bearer" }
```

Lỗi: 401 nếu sai thông tin.

## Dữ liệu

- Token JWT (HS256), thời hạn mặc định 24 giờ (`JWT_EXPIRATION_MS`).
- Chỉ lưu ở client (`sessionStorage`), không gửi qua URL.

## Giao diện

- `dashboard.html`: thêm overlay đăng nhập, nút đăng xuất ở header.
- Khi chưa login, ẩn nội dung dashboard.
- Khi login thành công, gọi API với header Bearer.

## Kiểm thử chấp nhận

1. Mở dashboard chưa login → hiện form đăng nhập.
2. Login đúng → dashboard hiện dữ liệu.
3. Login sai → thông báo lỗi, không lưu token.
4. Token hết hạn → quay lại form đăng nhập.
5. Đăng xuất → form đăng nhập, dữ liệu bị xoá.

## Rủi ro

- Token lưu client-side; XSS có thể đánh cắp. Chỉ dùng HTTPS ở production.
- `sessionStorage` mất khi đóng tab; admin phải login lại. Chấp nhận được.