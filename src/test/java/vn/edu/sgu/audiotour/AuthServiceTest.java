package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.sgu.audiotour.dto.ChangePasswordRequest;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.entity.AdminAuth;
import vn.edu.sgu.audiotour.exception.TooManyRequestsException;
import vn.edu.sgu.audiotour.exception.UnauthorizedException;
import vn.edu.sgu.audiotour.repository.AdminAuthRepository;
import vn.edu.sgu.audiotour.security.JwtService;
import vn.edu.sgu.audiotour.security.LoginRateLimiter;
import vn.edu.sgu.audiotour.service.AuthService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "auth-service-test-secret-0123456789abcdef";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final LoginRateLimiter loginRateLimiter = new LoginRateLimiter();

    @Mock
    private AdminAuthRepository adminAuthRepository;

    @BeforeEach
    void setUp() {
        loginRateLimiter.reset();
    }

    @Test
    void testBlankAdminPasswordIsRejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "admin", "   "));

        assertTrue(ex.getMessage().contains("APP_ADMIN_PASSWORD"));
    }

    @Test
    void testBlankOrTooShortAdminUsernameIsRejected() {
        IllegalStateException blank = assertThrows(IllegalStateException.class,
                () -> new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "  ", "admin123"));
        assertTrue(blank.getMessage().contains("APP_ADMIN_USERNAME"));

        assertThrows(IllegalStateException.class,
                () -> new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "ab", "admin123"));
    }

    @Test
    void testLoginWithConfiguredCredentialsReturnsBearerToken() {
        AdminAuth admin = new AdminAuth(1, "admin", passwordEncoder.encode("admin123"), LocalDateTime.now());
        when(adminAuthRepository.findById(1)).thenReturn(Optional.of(admin));

        AuthService authService = new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "admin", "admin123");

        LoginResponse response = authService.login("admin", "admin123");

        assertEquals("Bearer", response.getTokenType());
        assertFalse(response.getToken().isBlank());
    }

    @Test
    void testLoginWithWrongPasswordThrowsUnauthorized() {
        AdminAuth admin = new AdminAuth(1, "admin", passwordEncoder.encode("admin123"), LocalDateTime.now());
        when(adminAuthRepository.findById(1)).thenReturn(Optional.of(admin));

        AuthService authService = new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "admin", "admin123");

        assertThrows(UnauthorizedException.class, () -> authService.login("admin", "wrongpass", "1.2.3.4"));
    }

    @Test
    void testRateLimitBlocksAfter5Attempts() {
        AdminAuth admin = new AdminAuth(1, "admin", passwordEncoder.encode("admin123"), LocalDateTime.now());
        when(adminAuthRepository.findById(1)).thenReturn(Optional.of(admin));

        AuthService authService = new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "admin", "admin123");

        for (int i = 0; i < 5; i++) {
            assertThrows(UnauthorizedException.class, () -> authService.login("admin", "wrongpass", "10.0.0.1"));
        }

        // Attempt thứ 6 bị chặn bởi rate limit (429)
        assertThrows(TooManyRequestsException.class, () -> authService.login("admin", "admin123", "10.0.0.1"));
    }

    @Test
    void testChangePasswordSuccess() {
        AdminAuth admin = new AdminAuth(1, "admin", passwordEncoder.encode("admin123"), LocalDateTime.now());
        when(adminAuthRepository.findById(1)).thenReturn(Optional.of(admin));
        when(adminAuthRepository.save(any(AdminAuth.class))).thenAnswer(i -> i.getArgument(0));

        AuthService authService = new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "admin", "admin123");

        authService.changePassword("admin", new ChangePasswordRequest("admin123", "newPassword456"));

        assertTrue(passwordEncoder.matches("newPassword456", admin.getPasswordHash()));
    }

    @Test
    void testChangePasswordWithWrongOldPasswordThrowsUnauthorized() {
        AdminAuth admin = new AdminAuth(1, "admin", passwordEncoder.encode("admin123"), LocalDateTime.now());
        when(adminAuthRepository.findById(1)).thenReturn(Optional.of(admin));

        AuthService authService = new AuthService(jwtService, adminAuthRepository, passwordEncoder, loginRateLimiter, "admin", "admin123");

        assertThrows(UnauthorizedException.class,
                () -> authService.changePassword("admin", new ChangePasswordRequest("wrongOld", "newPassword456")));
    }
}
