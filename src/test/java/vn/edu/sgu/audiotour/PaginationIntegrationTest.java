package vn.edu.sgu.audiotour;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String bearer;

    @BeforeEach
    void login() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        bearer = "Bearer " + JsonPath.read(body, "$.token");
    }

    @Test
    void testLegacyLocationsStillReturnsPlainArray() throws Exception {
        mockMvc.perform(get("/api/locations").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$.totalElements").doesNotExist());
    }

    @Test
    void testV1LocationsIsPaged() throws Exception {
        mockMvc.perform(get("/api/v1/locations").header("Authorization", bearer)
                        .param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void testV1LocationsHonoursPageParameter() throws Exception {
        Long firstIdOnPage0 = firstContentId("/api/v1/locations", 0);
        Long firstIdOnPage1 = firstContentId("/api/v1/locations", 1);

        assertNotEquals(firstIdOnPage0, firstIdOnPage1);
    }

    @Test
    void testV1LanguagesIsPaged() throws Exception {
        mockMvc.perform(get("/api/v1/languages").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    private Long firstContentId(String path, int page) throws Exception {
        String body = mockMvc.perform(get(path).header("Authorization", bearer)
                        .param("page", String.valueOf(page)).param("size", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.content[0].id");
        return id.longValue();
    }

    private static void assertNotEquals(Long a, Long b) {
        org.junit.jupiter.api.Assertions.assertNotEquals(a, b);
    }
}
