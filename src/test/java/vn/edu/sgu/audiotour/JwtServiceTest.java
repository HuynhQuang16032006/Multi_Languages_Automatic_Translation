package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import vn.edu.sgu.audiotour.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "dev-secret-key-for-unit-tests-0123456789abcdef";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

    @Test
    void testGenerateAndParseToken() {
        String token = jwtService.generateToken("admin");

        assertNotNull(token);
        assertEquals("admin", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void credentialChangeRevokesTokenIncludingLegacyToken() {
        String token = jwtService.generateToken("admin", "old-hash");
        assertTrue(jwtService.isTokenValid(token, "admin", "old-hash"));
        assertFalse(jwtService.isTokenValid(token, "admin", "new-hash"));
        assertFalse(jwtService.isTokenValid(token, "other", "old-hash"));
        assertFalse(jwtService.isTokenValid(jwtService.generateToken("admin"), "admin", "old-hash"));
    }

    @Test
    void testTamperedTokenIsRejected() {
        String token = jwtService.generateToken("admin");
        String[] parts = token.split("\\.");

        // Đổi 1 ký tự trong phần payload để chữ ký không còn khớp
        char first = parts[1].charAt(0) == 'e' ? 'f' : 'e';
        String tampered = parts[0] + "." + first + parts[1].substring(1) + "." + parts[2];

        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void testTokenWithAppendedSignatureIsRejected() {
        String token = jwtService.generateToken("admin");

        assertFalse(jwtService.isTokenValid(token + "x"));
    }

    @Test
    void testTokenSignedWithAnotherSecretIsRejected() {
        JwtService otherSecret = new JwtService("other-secret-key-for-unit-tests-0123456789", 3_600_000L);
        String token = otherSecret.generateToken("admin");

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void testExpiredTokenIsRejected() {
        JwtService alreadyExpired = new JwtService(SECRET, -1_000L);
        String token = alreadyExpired.generateToken("admin");

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void testBlankOrShortSecretIsRejected() {
        IllegalStateException blank = assertThrows(IllegalStateException.class,
                () -> new JwtService("", 3_600_000L));
        assertTrue(blank.getMessage().contains("JWT_SECRET"));

        assertThrows(IllegalStateException.class, () -> new JwtService("   ", 3_600_000L));
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 3_600_000L));
    }

    @Test
    void testValidSecretIsAccepted() {
        JwtService valid = new JwtService(SECRET, 3_600_000L);

        assertNotNull(valid.generateToken("admin"));
    }
}
