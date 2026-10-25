package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackLogRecorderTest {

    @Mock
    private PlaybackLogRepository playbackLogRepository;

    @Mock
    private LocationRepository locationRepository;

    private MutableClock clock;
    private PlaybackLogRecorder recorder;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-25T00:00:00Z"));
        recorder = new PlaybackLogRecorder(playbackLogRepository, locationRepository, clock);

        Location poi = new Location();
        poi.setId(7L);
        poi.setName("Dinh Độc Lập");
        poi.setLatitude(10.7770);
        poi.setLongitude(106.6953);
        lenient().when(locationRepository.findById(7L)).thenReturn(Optional.of(poi));
        lenient().when(playbackLogRepository.save(any(PlaybackLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shortListenIsFlooredToFiveSeconds() {
        PlaybackLog saved = recorder.record(entry(0));

        assertEquals(5, saved.getListenDurationSeconds());
    }

    @Test
    void longerListenIsKept() {
        PlaybackLog saved = recorder.record(entry(65));

        assertEquals(65, saved.getListenDurationSeconds());
    }

    @Test
    void missingCoordinatesFallBackToPoi() {
        PlaybackLog saved = recorder.record(entry(65));

        assertEquals(10.7770, saved.getLatitude());
        assertEquals(106.6953, saved.getLongitude());
    }

    @Test
    void requestCoordinatesWinOverPoi() {
        PlaybackLog saved = recorder.record(new PlaybackLogRecorder.Entry(
                7L, "vi", 65, TriggerType.GEOFENCE, "device-a", 10.1, 106.2));

        assertEquals(10.1, saved.getLatitude());
        assertEquals(106.2, saved.getLongitude());
    }

    @Test
    void missingTriggerDefaultsToQrCode() {
        PlaybackLog saved = recorder.record(new PlaybackLogRecorder.Entry(
                7L, "vi", 65, null, "device-a", null, null));

        assertEquals(TriggerType.QR_CODE, saved.getTriggerType());
    }

    @Test
    void createdAtComesFromInjectedClock() {
        PlaybackLog saved = recorder.record(entry(65));

        assertEquals(LocalDateTime.of(2026, 10, 25, 0, 0), saved.getCreatedAt());
    }

    @Test
    void unknownPoiThrowsNotFoundAndWritesNothing() {
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> recorder.record(new PlaybackLogRecorder.Entry(
                999L, "vi", 65, TriggerType.QR_CODE, "device-a", null, null)));
        verify(playbackLogRepository, never()).save(any());
    }

    private PlaybackLogRecorder.Entry entry(int listenSeconds) {
        return new PlaybackLogRecorder.Entry(7L, "vi", listenSeconds, TriggerType.QR_CODE, "device-a", null, null);
    }
}
