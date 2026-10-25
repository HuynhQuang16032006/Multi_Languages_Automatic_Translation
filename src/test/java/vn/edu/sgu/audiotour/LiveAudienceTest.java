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
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveAudienceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PlaybackLogRecorder playbackLogRecorder;

    private MutableClock clock;
    private LiveAudience liveAudience;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-23T00:00:00Z"));
        liveAudience = new LiveAudience(locationRepository, playbackLogRecorder, clock);

        Location poi = new Location();
        poi.setId(7L);
        poi.setName("Dinh Độc Lập");
        poi.setLatitude(10.7770);
        poi.setLongitude(106.6953);
        // Dùng ở cả lúc join (lấy tên POI) và lúc ghi log (lấy tọa độ)
        lenient().when(locationRepository.findById(7L)).thenReturn(Optional.of(poi));
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
    void heartbeatWithoutLocationIsRejected() {
        DeviceHeartbeatRequest request = heartbeat("device-a", "JOIN");
        request.setLocationId(null);

        assertThrows(IllegalArgumentException.class, () -> liveAudience.record(request));
        assertTrue(liveAudience.snapshot().isEmpty());
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

        assertThrows(NotFoundException.class, () -> liveAudience.record(request));
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
