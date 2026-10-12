package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PlaybackLogRepository playbackLogRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public PlaybackLog recordPlaybackLog(PlaybackLogRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm với ID: " + request.getLocationId()));

        PlaybackLog log = new PlaybackLog();
        log.setLocation(location);
        log.setLanguageCode(request.getLanguageCode() != null ? request.getLanguageCode() : "vi");
        log.setListenDurationSeconds(request.getListenDurationSeconds() != null ? request.getListenDurationSeconds() : 0);
        log.setTriggerType(request.getTriggerType());
        log.setLatitude(request.getLatitude() != null ? request.getLatitude() : location.getLatitude());
        log.setLongitude(request.getLongitude() != null ? request.getLongitude() : location.getLongitude());
        log.setDeviceId(request.getDeviceId());
        log.setCreatedAt(LocalDateTime.now());

        return playbackLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        Long totalListens = playbackLogRepository.count();
        Double avgDuration = playbackLogRepository.getAverageListenDuration();
        if (avgDuration == null) {
            avgDuration = 0.0;
        }

        // 1. Thống kê theo phương thức kích hoạt (QR_CODE vs GEOFENCE)
        Map<String, Long> listensByTriggerType = new HashMap<>();
        List<Object[]> triggerResults = playbackLogRepository.countListensByTriggerType();
        for (Object[] row : triggerResults) {
            if (row[0] != null) {
                listensByTriggerType.put(row[0].toString(), (Long) row[1]);
            }
        }

        // 2. Top 5 địa điểm được nghe nhiều nhất
        List<Object[]> topRows = playbackLogRepository.getTopListenedLocations(PageRequest.of(0, 5));
        List<DashboardResponse.TopLocationStat> topLocations = topRows.stream()
                .map(row -> new DashboardResponse.TopLocationStat((Long) row[0], (String) row[1], (Long) row[2]))
                .collect(Collectors.toList());

        // 3. Danh sách tọa độ Heatmap
        List<Object[]> heatmapRows = playbackLogRepository.getHeatmapPoints();
        List<DashboardResponse.HeatmapPoint> heatmapPoints = heatmapRows.stream()
                .map(row -> new DashboardResponse.HeatmapPoint((Double) row[0], (Double) row[1], (Long) row[2]))
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalListens(totalListens)
                .averageListenDurationSeconds(Math.round(avgDuration * 10.0) / 10.0)
                .listensByTriggerType(listensByTriggerType)
                .topLocations(topLocations)
                .heatmapPoints(heatmapPoints)
                .build();
    }
}
