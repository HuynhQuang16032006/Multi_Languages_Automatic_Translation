package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
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

    @InjectMocks
    private AnalyticsService analyticsService;

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
        when(liveAudience.snapshot()).thenReturn(List.of());

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
