package vn.edu.sgu.audiotour;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Du khách dùng hệ thống ẩn danh: các API đọc phải public, API gì vẫn chỉ admin.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublicReadAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetLocationsWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPoisWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/locations/pois"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].latitude").exists());
    }

    @Test
    void testGetToursWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetLanguagesWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSupportedLanguagesWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/languages/supported"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").exists());
    }

    @Test
    void testAddLanguageWithTranslationIsPublic() throws Exception {
        // Mã ngoài whitelist -> 400 trước khi gọi DeepL: test không tốn credit và không ghi DB
        mockMvc.perform(post("/api/languages/zz/translate"))
                .andExpect(result -> assertNotEquals(HttpServletResponse.SC_UNAUTHORIZED,
                        result.getResponse().getStatus()));
    }

    @Test
    void testGetV1LocationsWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetNarrationWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/narrations")
                        .param("locationId", "5")
                        .param("languageCode", "vi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.textContent").isNotEmpty());
    }

    // Chặn hồi quy: thiếu @EntityGraph ở findByParentLocationId làm endpoint này 400 khi serialize JSON
    @Test
    void testGetSubLocationsWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/locations/4/sub-locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").isNotEmpty());
    }

    @Test
    void testCreateLanguageWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/languages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGenerateAudioWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/narrations/1/audio"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAnalyticsDashboardWithoutTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk());
    }
}
