package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
import vn.edu.sgu.audiotour.repository.PackageDownloadRepository;
import vn.edu.sgu.audiotour.service.AnalyticsService;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.ServerAddress;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PlaybackLogRepository playbackLogRepository;

    @Mock
    private LiveAudience liveAudience;

    @Mock
    private ServerAddress serverAddress;

    @Mock
    private PackageDownloadRepository packageDownloadRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testGetDashboardData() {
        Location location = new Location();
        location.setId(1L);
        location.setName("Dinh Độc Lập");
        List<PlaybackLog> logs = new ArrayList<>();
        for (int i = 0; i < 60; i++) logs.add(log(location, TriggerType.QR_CODE, 75));
        for (int i = 0; i < 40; i++) logs.add(log(location, TriggerType.GEOFENCE, 76));
        when(playbackLogRepository.findForDashboard(null, null, null)).thenReturn(logs);

        when(liveAudience.snapshot()).thenReturn(List.of());
        when(packageDownloadRepository.count()).thenReturn(0L);

        DashboardResponse response = analyticsService.getDashboardData();

        assertNotNull(response);
        assertEquals(100L, response.getTotalListens());
        assertEquals(75.4, response.getAverageListenDurationSeconds());
        assertEquals(60L, response.getListensByTriggerType().get("QR_CODE"));
        assertEquals(1, response.getTopLocations().size());
        assertEquals("Dinh Độc Lập", response.getTopLocations().get(0).getLocationName());
        assertEquals(1, response.getHeatmapPoints().size());
    }

    private PlaybackLog log(Location location, TriggerType trigger, int duration) {
        PlaybackLog log = new PlaybackLog();
        log.setLocation(location);
        log.setTriggerType(trigger);
        log.setLanguageCode("vi");
        log.setListenDurationSeconds(duration);
        log.setDeviceId("device-" + duration + trigger);
        log.setLatitude(10.7770);
        log.setLongitude(106.6953);
        return log;
    }
}
