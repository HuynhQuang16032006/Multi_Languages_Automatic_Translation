package vn.edu.sgu.audiotour;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.sgu.audiotour.repository.LanguageRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm tra CRUD ngôn ngữ với DB thật: ràng buộc FK khi xóa ngôn ngữ đang được tham chiếu.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LanguageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LanguageRepository languageRepository;

    private String bearer() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    @Test
    void testDeleteLanguageUsedByNarrationsReturns409() throws Exception {
        Long viId = languageRepository.findByCode("vi").orElseThrow().getId();

        mockMvc.perform(delete("/api/languages/" + viId).header("Authorization", bearer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("thuyết minh")));
    }

    @Test
    void testDeleteUnreferencedLanguageReturns204() throws Exception {
        String token = bearer();
        String body = mockMvc.perform(post("/api/languages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"zz\",\"name\":\"Ngon ngu tam\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(delete("/api/languages/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
    }
}
