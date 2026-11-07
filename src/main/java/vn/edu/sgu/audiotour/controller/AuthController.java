package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.dto.ChangePasswordRequest;
import vn.edu.sgu.audiotour.dto.LoginRequest;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "0. Xác thực (Auth Controller)", description = "API đăng nhập cấp JWT token và đổi mật khẩu admin. Dùng token này cho header Authorization: Bearer <token>")
public class AuthController {

    private final AuthService authService;

    @PostMapping({"/auth/login", "/v1/auth/login"})
    @Operation(summary = "Đăng nhập lấy JWT token", description = "Trả về 200 kèm token khi đúng thông tin đăng nhập, trả về 401 khi sai, 429 khi sai quá 5 lần")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword(), clientIp));
    }

    @PostMapping({"/auth/change-password", "/v1/auth/change-password"})
    @Operation(summary = "Đổi mật khẩu admin", description = "Yêu cầu Bearer token. Cập nhật mật khẩu hash trong DB.")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        authService.changePassword(username, request);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
