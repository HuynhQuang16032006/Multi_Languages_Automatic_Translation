package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.dto.DeviceHeartbeatRequest;
import vn.edu.sgu.audiotour.dto.HeartbeatResponse;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveAudienceTest {

    @Mock
    private LocationService locationService;

    @Mock
    private PlaybackLogRecorder playbackLogRecorder;

    private MutableClock clock;
    private LiveAudience liveAudience;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-23T00:00:00Z"));
        liveAudience = new LiveAudience(locationService, playbackLogRecorder, clock);

        Location poi = new Location();
        poi.setId(7L);
        poi.setName("Dinh Độc Lập");
        poi.setLatitude(10.7770);
        poi.setLongitude(106.6953);
        lenient().when(locationService.getLocationById(7L)).thenReturn(poi);
    }

    @Test
    void joinThenPingCountsDeviceOnce() {
        assertEquals(1, liveAudience.record(heartbeat("device-a", "JOIN")).activeUsers());

        clock.advanceMillis(3_000);
        assertEquals(1, liveAudience.record(heartbeat("device-a", "PING")).activeUsers());
        assertEquals(1, liveAudience.snapshot().size());
    }

    @Test
    void twoDevicesCountTwo() {
        liveAudience.record(heartbeat("device-a", "JOIN"));

        assertEquals(2, liveAudience.record(heartbeat("device-b", "JOIN")).activeUsers());
    }

    @Test
    void heartbeatWithoutLocationIsAcceptedAsBrowsing() {
        DeviceHeartbeatRequest request = heartbeat("device-a", "JOIN");
        request.setLocationId(null);

        assertEquals(1, liveAudience.record(request).activeUsers());
        assertEquals("Đang xem bản đồ", liveAudience.snapshot().get(0).locationName());
    }

    @Test
    void heartbeatWithoutLanguageIsRejected() {
        DeviceHeartbeatRequest request = heartbeat("device-a", "JOIN");
        request.setLanguageCode(" ");

        assertThrows(IllegalArgumentException.class, () -> liveAudience.record(request));
        assertTrue(liveAudience.snapshot().isEmpty());
    }

    @Test
    void heartbeatForUnknownPoiIsRejected() {
        DeviceHeartbeatRequest request = heartbeat("device-a", "JOIN");
        request.setLocationId(999L);
        when(locationService.getLocationById(999L)).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> liveAudience.record(request));
        assertThrows(NotFoundException.class, () -> {
            request.setAction("PING");
            liveAudience.record(request);
        });
        assertTrue(liveAudience.snapshot().isEmpty());
    }

    @Test
    void unknownActionIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> liveAudience.record(heartbeat("device-a", "LEAV")));
        assertTrue(liveAudience.snapshot().isEmpty());
    }

    @Test
    void leaveWritesOneLogAndRemovesSession() {
        liveAudience.record(heartbeat("device-a", "JOIN"));
        clock.advanceMillis(4_000);

        LiveAudience.HeartbeatResult result = liveAudience.record(heartbeat("device-a", "LEAVE"));

        assertEquals(HeartbeatResponse.Status.LEFT, result.status());
        assertEquals(0, result.activeUsers());
        assertTrue(liveAudience.snapshot().isEmpty());
        verify(playbackLogRecorder, times(1)).record(any(PlaybackLogRecorder.Entry.class));
    }

    @Test
    void manualTriggerIsWrittenToPlaybackLog() {
        DeviceHeartbeatRequest join = heartbeat("device-a", "JOIN");
        join.setTriggerType(TriggerType.MANUAL);
        liveAudience.record(join);
        liveAudience.record(heartbeat("device-a", "LEAVE"));

        ArgumentCaptor<PlaybackLogRecorder.Entry> captor = ArgumentCaptor.forClass(PlaybackLogRecorder.Entry.class);
        verify(playbackLogRecorder).record(captor.capture());
        assertEquals(TriggerType.MANUAL, captor.getValue().triggerType());
    }

    @Test
    void secondLeaveWritesNothing() {
        liveAudience.record(heartbeat("device-a", "JOIN"));
        liveAudience.record(heartbeat("device-a", "LEAVE"));
        liveAudience.record(heartbeat("device-a", "LEAVE"));

        verify(playbackLogRecorder, times(1)).record(any(PlaybackLogRecorder.Entry.class));
    }

    @Test
    void expiryWaitsForTimeoutThenWritesExactlyOnce() {
        liveAudience.record(heartbeat("device-a", "JOIN"));

        clock.advanceMillis(7_000);
        assertEquals(1, liveAudience.snapshot().size());
        verify(playbackLogRecorder, never()).record(any());

        clock.advanceMillis(1_001);
        assertTrue(liveAudience.snapshot().isEmpty());
        verify(playbackLogRecorder, times(1)).record(any(PlaybackLogRecorder.Entry.class));

        clock.advanceMillis(1_000);
        assertTrue(liveAudience.snapshot().isEmpty());
        verifyNoMoreInteractions(playbackLogRecorder);
    }

    @Test
    void logFailureDoesNotKeepSessionAlive() {
        liveAudience.record(heartbeat("device-a", "JOIN"));
        when(playbackLogRecorder.record(any(PlaybackLogRecorder.Entry.class)))
                .thenThrow(new RuntimeException("DB down"));

        clock.advanceMillis(8_001);

        assertTrue(liveAudience.snapshot().isEmpty());
        assertEquals(1, liveAudience.record(heartbeat("device-b", "JOIN")).activeUsers());
    }

    @Test
    void hiddenLocationPreservesExistingSessionButRejectsNewSession() {
        DeviceHeartbeatRequest request = heartbeat("device-a", "JOIN");
        liveAudience.record(request);
        when(locationService.getLocationById(7L)).thenThrow(new NotFoundException("hidden"));
        request.setAction("PING");
        assertEquals(HeartbeatResponse.Status.ACTIVE, liveAudience.record(request).status());
        assertThrows(NotFoundException.class, () -> liveAudience.record(heartbeat("device-b", "PING")));
        assertThrows(NotFoundException.class, () -> liveAudience.record(heartbeat("device-c", "JOIN")));
        request.setAction("LEAVE");
        assertEquals(HeartbeatResponse.Status.LEFT, liveAudience.record(request).status());
        verify(playbackLogRecorder).record(any());
    }

    private DeviceHeartbeatRequest heartbeat(String deviceId, String action) {
        DeviceHeartbeatRequest request = new DeviceHeartbeatRequest();
        request.setDeviceId(deviceId);
        request.setLocationId(7L);
        request.setLanguageCode("vi");
        request.setAction(action);
        request.setListenDurationSeconds(30);
        return request;
    }
}
