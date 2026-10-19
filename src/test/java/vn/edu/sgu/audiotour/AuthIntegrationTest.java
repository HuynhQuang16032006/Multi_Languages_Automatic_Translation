package vn.edu.sgu.audiotour;

import com.jayway.jsonpath.JsonPath;
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

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void testGetLocationsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isUnauthorized());
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
        mockMvc.perform(get("/api/locations").header("Authorization", "Bearer khong-phai-token"))
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
