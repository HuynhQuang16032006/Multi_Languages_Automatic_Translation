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
import vn.edu.sgu.audiotour.controller.AudioPackageController;
import vn.edu.sgu.audiotour.dto.DeviceHeartbeatRequest;
import vn.edu.sgu.audiotour.dto.HeartbeatResponse;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.exception.GlobalExceptionHandler;
import vn.edu.sgu.audiotour.service.AnalyticsService;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.NarrationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerValidationTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private LiveAudience liveAudience;

    @Mock
    private PlaybackLogRecorder playbackLogRecorder;

    @Mock
    private LocationService locationService;

    @Mock
    private NarrationService narrationService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController, new AudioPackageController(narrationService, locationService))
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

        when(playbackLogRecorder.record(any(PlaybackLogRecorder.Entry.class))).thenReturn(new PlaybackLog());

        mockMvc.perform(post("/api/analytics/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void testHeartbeatReturnsTypedResponse() throws Exception {
        when(liveAudience.record(any(DeviceHeartbeatRequest.class)))
                .thenReturn(new LiveAudience.HeartbeatResult(HeartbeatResponse.Status.ACTIVE, "device-a", 2));

        mockMvc.perform(post("/api/analytics/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":\"device-a\",\"locationId\":7,\"languageCode\":\"vi\",\"action\":\"JOIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.deviceId").value("device-a"))
                .andExpect(jsonPath("$.activeUsers").value(2));
    }

    @Test
    void testHeartbeatWithUnknownActionReturns400() throws Exception {
        when(liveAudience.record(any(DeviceHeartbeatRequest.class)))
                .thenThrow(new IllegalArgumentException("Action không hợp lệ"));

        mockMvc.perform(post("/api/analytics/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":\"device-a\",\"locationId\":7,\"languageCode\":\"vi\",\"action\":\"LEAV\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testLongDeviceIdReturns400AtEachBoundary() throws Exception {
        String deviceId = "x".repeat(65);
        mockMvc.perform(post("/api/analytics/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":\"" + deviceId + "\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/analytics/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":1,\"languageCode\":\"vi\",\"listenDurationSeconds\":1,\"deviceId\":\"" + deviceId + "\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/packages/offline")
                        .param("locationId", "1").param("languageCode", "vi").param("deviceId", deviceId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMaximumLengthAndNullDeviceIdRemainAccepted() throws Exception {
        when(liveAudience.record(any(DeviceHeartbeatRequest.class)))
                .thenReturn(new LiveAudience.HeartbeatResult(HeartbeatResponse.Status.ACTIVE, "device-a", 1));
        when(playbackLogRecorder.record(any(PlaybackLogRecorder.Entry.class))).thenReturn(new PlaybackLog());
        when(narrationService.createOfflinePackage(org.mockito.ArgumentMatchers.anyLong(), any(), org.mockito.ArgumentMatchers.nullable(String.class)))
                .thenReturn(new byte[0]);
        for (String deviceId : new String[]{"x".repeat(64), null}) {
            String value = deviceId == null ? "null" : "\"" + deviceId + "\"";
            mockMvc.perform(post("/api/analytics/heartbeat").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"deviceId\":" + value + "}"))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/analytics/logs").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"locationId\":1,\"languageCode\":\"vi\",\"listenDurationSeconds\":1,\"deviceId\":" + value + "}"))
                    .andExpect(status().isOk());
            var request = get("/api/packages/offline").param("locationId", "1").param("languageCode", "vi");
            if (deviceId != null) request.param("deviceId", deviceId);
            mockMvc.perform(request).andExpect(status().isOk());
        }
    }
}
