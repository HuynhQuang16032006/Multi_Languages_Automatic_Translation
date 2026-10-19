package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.dto.LoginRequest;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.service.AuthService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "0. Xác thực (Auth Controller)", description = "API đăng nhập cấp JWT token cho Mobile App / CMS. Dùng token này cho header Authorization: Bearer <token>")
public class AuthController {

    private final AuthService authService;

    @PostMapping({"/auth/login", "/v1/auth/login"})
    @Operation(summary = "Đăng nhập lấy JWT token", description = "Trả về 200 kèm token khi đúng thông tin đăng nhập, trả về 401 khi sai")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));
    }
}
