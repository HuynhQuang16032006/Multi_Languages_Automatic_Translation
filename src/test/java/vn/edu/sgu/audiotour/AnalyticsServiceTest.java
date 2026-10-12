package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
import vn.edu.sgu.audiotour.service.AnalyticsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PlaybackLogRepository playbackLogRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testRecordPlaybackLog_Success() {
        Location location = new Location();
        location.setId(1L);
        location.setLatitude(10.7770);
        location.setLongitude(106.6953);

        PlaybackLogRequest req = new PlaybackLogRequest();
        req.setLocationId(1L);
        req.setLanguageCode("vi");
        req.setListenDurationSeconds(80);
        req.setTriggerType(TriggerType.QR_CODE);
        req.setDeviceId("device-test-123");

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(playbackLogRepository.save(any(PlaybackLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaybackLog saved = analyticsService.recordPlaybackLog(req);

        assertNotNull(saved);
        assertEquals(80, saved.getListenDurationSeconds());
        assertEquals(TriggerType.QR_CODE, saved.getTriggerType());
        verify(playbackLogRepository, times(1)).save(any(PlaybackLog.class));
    }

    @Test
    void testGetDashboardData() {
        when(playbackLogRepository.count()).thenReturn(100L);
        when(playbackLogRepository.getAverageListenDuration()).thenReturn(75.5);

        List<Object[]> triggerResults = new ArrayList<>();
        triggerResults.add(new Object[]{TriggerType.QR_CODE, 60L});
        triggerResults.add(new Object[]{TriggerType.GEOFENCE, 40L});
        when(playbackLogRepository.countListensByTriggerType()).thenReturn(triggerResults);

        List<Object[]> topRows = new ArrayList<>();
        topRows.add(new Object[]{1L, "Dinh Độc Lập", 50L});
        when(playbackLogRepository.getTopListenedLocations(any(Pageable.class))).thenReturn(topRows);

        List<Object[]> heatmapRows = new ArrayList<>();
        heatmapRows.add(new Object[]{10.7770, 106.6953, 50L});
        when(playbackLogRepository.getHeatmapPoints()).thenReturn(heatmapRows);

        DashboardResponse response = analyticsService.getDashboardData();

        assertNotNull(response);
        assertEquals(100L, response.getTotalListens());
        assertEquals(75.5, response.getAverageListenDurationSeconds());
        assertEquals(60L, response.getListensByTriggerType().get("QR_CODE"));
        assertEquals(1, response.getTopLocations().size());
        assertEquals("Dinh Độc Lập", response.getTopLocations().get(0).getLocationName());
        assertEquals(1, response.getHeatmapPoints().size());
    }
}
