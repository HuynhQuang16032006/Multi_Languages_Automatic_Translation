package vn.edu.sgu.audiotour;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.sgu.audiotour.security.JwtAuthenticationFilter;
import vn.edu.sgu.audiotour.security.LoginRateLimiter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @BeforeEach
    void setUp() {
        loginRateLimiter.reset();
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void testGetLocationsWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"sai-mat-khau\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithCorrectCredentialsReturnsToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testGetLocationsWithBearerTokenReturns200() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/locations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void testInvalidBearerTokenReturns401() throws Exception {
        // Endpoint đọc đã public nên token sai cũng không chặn được; kiểm tra trên endpoint vẫn cần token
        mockMvc.perform(get("/actuator/metrics").header("Authorization", "Bearer khong-phai-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testActuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testActuatorMetricsRequiresToken() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePasswordWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"admin123\",\"newPassword\":\"newPass123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePasswordSuccessAndLoginWithNewPassword() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"wrongPassword\",\"newPassword\":\"newSecret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mật khẩu cũ không chính xác"));

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"admin123\",\"newPassword\":\"newSecret123\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/metrics").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        // Đăng nhập bằng mật khẩu mới
        String newToken = login("admin", "newSecret123");
        assertNotNull(newToken);

        // Đổi lại về mật khẩu cũ để không ảnh hưởng test khác
        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + newToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"newSecret123\",\"newPassword\":\"admin123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testRateLimitReturns429AfterFiveFailedLogins() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", "192.168.1." + i)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"admin\",\"password\":\"sai\"}"))
                    .andExpect(status().isUnauthorized());
        }

        // Lần thứ 6 trả 429 Too Many Requests
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "203.0.113.1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void testJwtFilterRunsOnceAndStaysInSecurityChain() {
        assertTrue(applicationContext.getBeansOfType(JwtAuthenticationFilter.class).isEmpty(),
                "JwtAuthenticationFilter không được là bean: Boot sẽ đăng ký thêm 1 lần như servlet filter và chạy 2 lần/request");

        List<SecurityFilterChain> chains = applicationContext.getBeansOfType(SecurityFilterChain.class)
                .values().stream().toList();
        assertFalse(chains.isEmpty());

        boolean inChain = chains.stream()
                .filter(DefaultSecurityFilterChain.class::isInstance)
                .map(DefaultSecurityFilterChain.class::cast)
                .flatMap(chain -> chain.getFilters().stream())
                .anyMatch(JwtAuthenticationFilter.class::isInstance);

        assertTrue(inChain, "JwtAuthenticationFilter vẫn phải nằm trong SecurityFilterChain");
    }
}
