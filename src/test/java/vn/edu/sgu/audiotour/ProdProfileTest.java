package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.service.AuthService;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Chứng minh profile `prod` thật sự được kích hoạt và đọc đúng các biến mà application-prod.yml yêu cầu.
 * Các "biến môi trường" ở đây được truyền qua property để test chạy được mà không cần export shell.
 */
@SpringBootTest(properties = {
        "DB_USERNAME=root",
        "DB_PASSWORD=123456",
        "JWT_SECRET=prod-profile-test-secret-0123456789abcdef",
        "APP_ADMIN_USERNAME=prodadmin",
        "APP_ADMIN_PASSWORD=prodpass"
})
@ActiveProfiles("prod")
class ProdProfileTest {

    @Autowired
    private AuthService authService;

    @Test
    void testProdProfileBootsAndUsesConfiguredAdminAccount() {
        LoginResponse response = authService.login("prodadmin", "prodpass");

        assertFalse(response.getToken().isBlank());
    }
}
