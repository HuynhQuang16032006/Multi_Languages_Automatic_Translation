package vn.edu.sgu.audiotour.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.exception.UnauthorizedException;
import vn.edu.sgu.audiotour.security.JwtService;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final String adminUsername;
    private final String adminPassword;

    public AuthService(JwtService jwtService,
                       @Value("${app.admin.username}") String adminUsername,
                       @Value("${app.admin.password}") String adminPassword) {
        // Fail-fast lúc khởi động: tài khoản không mật khẩu là lỗ hổng cấu hình
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalStateException(
                    "Thiếu biến môi trường APP_ADMIN_USERNAME: app.admin.username không được để trống");
        }
        if (adminUsername.trim().length() < 3) {
            throw new IllegalStateException(
                    "Biến môi trường APP_ADMIN_USERNAME phải dài tối thiểu 3 ký tự");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Thiếu biến môi trường APP_ADMIN_PASSWORD: app.admin.password không được để trống");
        }
        this.jwtService = jwtService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    // Kiểm tra tài khoản admin (lấy từ config) rồi cấp JWT token
    public LoginResponse login(String username, String password) {
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");
        }
        return new LoginResponse(jwtService.generateToken(username), "Bearer");
    }
}
