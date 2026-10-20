package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.sgu.audiotour.controller.NarrationController;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.exception.GlobalExceptionHandler;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.service.NarrationService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NarrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NarrationService narrationService;

    @InjectMocks
    private NarrationController narrationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(narrationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetNarration_Success() throws Exception {
        when(narrationService.getNarration(5L, "vi")).thenReturn(NarrationResponse.builder()
                .locationId(5L)
                .locationName("Dinh Độc Lập")
                .languageCode("vi")
                .textContent("Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập.")
                .audioUrl("dinh-doc-lap-vi.mp3")
                .durationInSeconds(95)
                .build());

        mockMvc.perform(get("/api/narrations")
                        .param("locationId", "5")
                        .param("languageCode", "vi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId").value(5L))
                .andExpect(jsonPath("$.locationName").value("Dinh Độc Lập"))
                .andExpect(jsonPath("$.languageCode").value("vi"))
                .andExpect(jsonPath("$.textContent").value("Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập."))
                .andExpect(jsonPath("$.audioUrl").value("dinh-doc-lap-vi.mp3"))
                .andExpect(jsonPath("$.durationInSeconds").value(95));
    }

    @Test
    void testGetNarration_NotFoundReturns404() throws Exception {
        when(narrationService.getNarration(999L, "vi"))
                .thenThrow(new NotFoundException("Không tìm thấy bài thuyết minh cho địa điểm 999 và ngôn ngữ vi"));

        mockMvc.perform(get("/api/narrations")
                        .param("locationId", "999")
                        .param("languageCode", "vi"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/narrations"));
    }

    @Test
    void testGetNarration_UnknownLanguageCodeReturns404() throws Exception {
        when(narrationService.getNarration(5L, "xx"))
                .thenThrow(new NotFoundException("Không tìm thấy bài thuyết minh cho địa điểm 5 và ngôn ngữ xx"));

        mockMvc.perform(get("/api/narrations")
                        .param("locationId", "5")
                        .param("languageCode", "xx"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/narrations"));
    }

    @Test
    void testGetNarration_MissingLocationIdReturns400() throws Exception {
        mockMvc.perform(get("/api/narrations")
                        .param("languageCode", "vi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testGetNarration_MissingLanguageCodeReturns400() throws Exception {
        mockMvc.perform(get("/api/narrations")
                        .param("locationId", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
