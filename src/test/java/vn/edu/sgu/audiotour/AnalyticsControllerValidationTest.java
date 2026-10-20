package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.sgu.audiotour.controller.AnalyticsController;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.exception.GlobalExceptionHandler;
import vn.edu.sgu.audiotour.service.AnalyticsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerValidationTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testInvalidPlaybackLogReturns400() throws Exception {
        String body = """
                {"languageCode":"","listenDurationSeconds":-5}
                """;

        mockMvc.perform(post("/api/analytics/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("locationId")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("languageCode")))
                .andExpect(jsonPath("$.path").value("/api/analytics/logs"));
    }

    @Test
    void testValidPlaybackLogReturns200() throws Exception {
        String body = """
                {"locationId":1,"languageCode":"vi","listenDurationSeconds":65,"triggerType":"QR_CODE"}
                """;

        when(analyticsService.recordPlaybackLog(any(PlaybackLogRequest.class))).thenReturn(new PlaybackLog());

        mockMvc.perform(post("/api/analytics/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
