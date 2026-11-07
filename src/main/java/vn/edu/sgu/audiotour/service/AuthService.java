package vn.edu.sgu.audiotour.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.ChangePasswordRequest;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.entity.AdminAuth;
import vn.edu.sgu.audiotour.exception.UnauthorizedException;
import vn.edu.sgu.audiotour.repository.AdminAuthRepository;
import vn.edu.sgu.audiotour.security.JwtService;
import vn.edu.sgu.audiotour.security.LoginRateLimiter;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final AdminAuthRepository adminAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;
    private final String adminUsername;
    private final String adminPassword;

    public AuthService(JwtService jwtService,
                       AdminAuthRepository adminAuthRepository,
                       PasswordEncoder passwordEncoder,
                       LoginRateLimiter loginRateLimiter,
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
        this.adminAuthRepository = adminAuthRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimiter = loginRateLimiter;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public void initAdmin() {
        adminAuthRepository.findById(1).ifPresentOrElse(
                existing -> {
                    // Nếu đổi biến môi trường username (ví dụ chuyển profile sang prodadmin), đồng bộ lại theo config
                    if (!existing.getUsername().equals(adminUsername)) {
                        existing.setUsername(adminUsername);
                        existing.setPasswordHash(passwordEncoder.encode(adminPassword));
                        existing.setUpdatedAt(LocalDateTime.now());
                        adminAuthRepository.save(existing);
                    }
                },
                () -> {
                    AdminAuth admin = new AdminAuth(
                            1,
                            adminUsername,
                            passwordEncoder.encode(adminPassword),
                            LocalDateTime.now()
                    );
                    adminAuthRepository.save(admin);
                }
        );
    }

    public LoginResponse login(String username, String password) {
        return login(username, password, "local");
    }

    // Kiểm tra tài khoản admin (DB + BCrypt) kèm rate-limit chống brute-force rồi cấp JWT token
    public LoginResponse login(String username, String password, String clientIp) {
        loginRateLimiter.checkLimit(clientIp);

        AdminAuth admin = adminAuthRepository.findById(1)
                .orElseGet(() -> {
                    AdminAuth fallback = new AdminAuth(
                            1,
                            adminUsername,
                            passwordEncoder.encode(adminPassword),
                            LocalDateTime.now()
                    );
                    return adminAuthRepository.save(fallback);
                });

        if (!admin.getUsername().equals(username) || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            loginRateLimiter.recordFailure(clientIp);
            throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");
        }

        loginRateLimiter.recordSuccess(clientIp);
        return new LoginResponse(jwtService.generateToken(username), "Bearer");
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        AdminAuth admin = adminAuthRepository.findById(1)
                .orElseThrow(() -> new UnauthorizedException("Không tìm thấy tài khoản admin"));

        if (!admin.getUsername().equals(username)) {
            throw new UnauthorizedException("Tài khoản không hợp lệ");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPasswordHash())) {
            throw new UnauthorizedException("Mật khẩu cũ không chính xác");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        admin.setUpdatedAt(LocalDateTime.now());
        adminAuthRepository.save(admin);
    }
}
