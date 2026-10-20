package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.security.JwtService;
import vn.edu.sgu.audiotour.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String SECRET = "auth-service-test-secret-0123456789abcdef";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

    @Test
    void testBlankAdminPasswordIsRejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new AuthService(jwtService, "admin", "   "));

        assertTrue(ex.getMessage().contains("APP_ADMIN_PASSWORD"));
    }

    @Test
    void testBlankOrTooShortAdminUsernameIsRejected() {
        IllegalStateException blank = assertThrows(IllegalStateException.class,
                () -> new AuthService(jwtService, "  ", "admin123"));
        assertTrue(blank.getMessage().contains("APP_ADMIN_USERNAME"));

        assertThrows(IllegalStateException.class, () -> new AuthService(jwtService, "ab", "admin123"));
    }

    @Test
    void testLoginWithConfiguredCredentialsReturnsBearerToken() {
        AuthService authService = new AuthService(jwtService, "admin", "admin123");

        LoginResponse response = authService.login("admin", "admin123");

        assertEquals("Bearer", response.getTokenType());
        assertFalse(response.getToken().isBlank());
    }
}
