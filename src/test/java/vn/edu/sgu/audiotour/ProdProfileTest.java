package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.sgu.audiotour.dto.LoginResponse;
import vn.edu.sgu.audiotour.service.AuthService;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Chứng minh profile `prod` thật sự được kích hoạt và dùng đúng tài khoản admin đã cấu hình.
 * Đặt thẳng app.admin.* vì placeholder ${APP_ADMIN_USERNAME} trong application-prod.yml được
 * resolve lúc load config, nên property inlined không thắng được biến môi trường có sẵn (vd. CI).
 * DB được cách ly bởi TestDatabaseIsolation.
 */
@SpringBootTest(properties = {
        "DB_USERNAME=root",
        "DB_PASSWORD=123456",
        "jwt.secret=prod-profile-test-secret-0123456789abcdef",
        "app.admin.username=prodadmin",
        "app.admin.password=prodpass"
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
