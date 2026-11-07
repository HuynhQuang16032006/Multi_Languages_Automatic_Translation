# Spec: Đăng nhập admin cho dashboard

## Objective

Thêm luồng đăng nhập admin cho `dashboard.html` để truy cập `/api/analytics/dashboard` và các API admin. Admin nhập username/password, nhận JWT, lưu client-side, gửi Bearer token trong request. Trang chủ du khách vẫn public.

## Tech Stack

- Backend: Java 21, Spring Boot 4.1.1, Spring Security, JWT (jjwt 0.12.6)
- Frontend: HTML tĩnh + vanilla JS trong `src/main/resources/static/`
- DB: MySQL 8.0+ (không thay đổi schema)
- Test: JUnit 5, MockMvc

## Commands

- Build: `.\mvnw.cmd compile`
- Test: `.\mvnw.cmd test`
- Run: `.\mvnw.cmd spring-boot:run`
- Lint: n/a (Java formatting via IDE)

## Project Structure

- `src/main/java/.../security/` — SecurityConfig, JwtAuthenticationFilter
- `src/main/java/.../controller/AuthController.java` — login endpoint
- `src/main/resources/static/dashboard.html` — dashboard UI
- `src/test/java/...` — controller/security tests

## Code Style

- Java: constructor injection với `@RequiredArgsConstructor`, DTO rõ ràng.
- Frontend: vanilla JS, fetch API, token trong `sessionStorage`.
- Naming: camelCase JS, PascalCase Java classes.

## Testing Strategy

- Unit: AuthService login success/failure.
- Controller: MockMvc test `/api/auth/login` (200/401).
- Security: request không token → 401; có token → 200.
- Manual: mở dashboard, login, xem dữ liệu, logout.

## Boundaries

- Always: không commit secret; token không qua URL; xử lý 401.
- Ask first: thay đổi security rules, thêm dependency.
- Never: lưu token trong localStorage dài hạn; bypass auth cho dashboard.

## Success Criteria

1. Mở `/dashboard.html` chưa login → hiện form đăng nhập.
2. Login đúng → dashboard hiện dữ liệu analytics.
3. Login sai → thông báo lỗi, không lưu token.
4. Token hết hạn/sai → quay lại form đăng nhập.
5. Đăng xuất → xoá token, hiện form.
6. Trang chủ du khách và API public vẫn hoạt động.

## Open Questions

- Dùng `sessionStorage` hay `localStorage`? (đề xuất `sessionStorage`)
- Có cần remember-me? (ngoài phạm vi)
- Dashboard có cần refresh token? (ngoài phạm vi)