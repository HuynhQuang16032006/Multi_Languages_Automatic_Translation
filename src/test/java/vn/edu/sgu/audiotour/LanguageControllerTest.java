package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.sgu.audiotour.controller.LanguageController;
import vn.edu.sgu.audiotour.dto.LanguageRequest;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.GlobalExceptionHandler;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.service.LanguageService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LanguageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private LanguageController languageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(languageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testGetAllLanguages() throws Exception {
        when(languageService.getAllLanguages()).thenReturn(List.of(new Language(1L, "vi", "Tiếng Việt")));

        mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("vi"))
                .andExpect(jsonPath("$[0].name").value("Tiếng Việt"));
    }

    @Test
    void testGetLanguagesPaged() throws Exception {
        when(languageService.getLanguages(any())).thenReturn(new PageImpl<>(
                List.of(new Language(1L, "vi", "Tiếng Việt")), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/languages").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("vi"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testGetLanguageById() throws Exception {
        when(languageService.getLanguageById(1L)).thenReturn(new Language(1L, "vi", "Tiếng Việt"));

        mockMvc.perform(get("/api/languages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetLanguageById_NotFoundReturns404() throws Exception {
        when(languageService.getLanguageById(999L)).thenThrow(new NotFoundException("Không tìm thấy ngôn ngữ với ID: 999"));

        mockMvc.perform(get("/api/languages/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testCreateLanguage_Returns201() throws Exception {
        when(languageService.createLanguage(any(LanguageRequest.class)))
                .thenReturn(new Language(3L, "fr", "Français"));

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"fr\",\"name\":\"Français\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.code").value("fr"));
    }

    @Test
    void testCreateLanguageV1_Returns201() throws Exception {
        when(languageService.createLanguage(any(LanguageRequest.class)))
                .thenReturn(new Language(3L, "fr", "Français"));

        mockMvc.perform(post("/api/v1/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"fr\",\"name\":\"Français\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateLanguage_InvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("code")));
    }

    @Test
    void testUpdateLanguage_Returns200() throws Exception {
        when(languageService.updateLanguage(eq(1L), any(LanguageRequest.class)))
                .thenReturn(new Language(1L, "en", "English"));

        mockMvc.perform(put("/api/languages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"en\",\"name\":\"English\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("en"));
    }

    @Test
    void testCreateLanguage_DuplicateCodeReturns409() throws Exception {
        when(languageService.createLanguage(any(LanguageRequest.class)))
                .thenThrow(new ConflictException("Mã ngôn ngữ đã tồn tại: vi"));

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"vi\",\"name\":\"Tiếng Việt\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("đã tồn tại")));
    }

    @Test
    void testCreateLanguage_RaceViolatesUniqueConstraintReturns409() throws Exception {
        // Mô phỏng race: pre-check qua nhưng unique constraint DB chặn -> không được lộ message SQL thô
        when(languageService.createLanguage(any(LanguageRequest.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry 'vi' for key 'uk_languages_code'"));

        mockMvc.perform(post("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"vi\",\"name\":\"Tiếng Việt\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Duplicate entry"))));
    }

    @Test
    void testDeleteLanguage_ReferencedReturns409() throws Exception {
        org.mockito.Mockito.doThrow(new ConflictException("Không thể xóa ngôn ngữ vì đang được dùng trong bản thuyết minh: 1"))
                .when(languageService).deleteLanguage(1L);

        mockMvc.perform(delete("/api/languages/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("thuyết minh")));
    }

    @Test
    void testDeleteLanguage_Returns204() throws Exception {
        mockMvc.perform(delete("/api/languages/1"))
                .andExpect(status().isNoContent());
    }
}
