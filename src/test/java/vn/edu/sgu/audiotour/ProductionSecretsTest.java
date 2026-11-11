package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import vn.edu.sgu.audiotour.config.ProductionSecrets;

import static org.junit.jupiter.api.Assertions.*;

class ProductionSecretsTest {
    @Test
    void rejectsDevelopmentDefaultsAndBlankSecrets() {
        String secret = "independent-production-test-secret-0123456789";
        assertDoesNotThrow(() -> new ProductionSecrets(secret, "admin-pass", "db-pass"));
        assertThrows(IllegalStateException.class, () -> new ProductionSecrets(
                "dev-secret-key-change-me-in-production-0123456789abcdef", "admin-pass", "db-pass"));
        assertThrows(IllegalStateException.class, () -> new ProductionSecrets(secret, "admin123", "db-pass"));
        assertThrows(IllegalStateException.class, () -> new ProductionSecrets(secret, "admin-pass", "123456"));
        assertThrows(IllegalStateException.class, () -> new ProductionSecrets(secret, "admin-pass", " "));
    }
}
