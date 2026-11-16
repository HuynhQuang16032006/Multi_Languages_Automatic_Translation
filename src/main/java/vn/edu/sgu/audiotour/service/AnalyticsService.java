package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
import vn.edu.sgu.audiotour.repository.PackageDownloadRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PlaybackLogRepository playbackLogRepository;
    private final LiveAudience liveAudience;
    private final ServerAddress serverAddress;
    private final PackageDownloadRepository packageDownloadRepository;

    @Transactional
    public DashboardResponse getDashboardData() {
        return getDashboardData(null, null, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(LocalDateTime from, LocalDateTime to, String languageCode) {
        List<PlaybackLog> logs = playbackLogRepository.findForDashboard(from, to, languageCode);
        Long totalListens = (long) logs.size();
        Double avgDuration = logs.stream().map(PlaybackLog::getListenDurationSeconds).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0.0);
        if (avgDuration == null) {
            avgDuration = 0.0;
        }

        // 1. Thống kê theo phương thức kích hoạt
        Map<String, Long> listensByTriggerType = new HashMap<>();
        logs.stream().filter(l -> l.getTriggerType() != null).collect(Collectors.groupingBy(
                l -> l.getTriggerType().name(), Collectors.counting())).forEach(listensByTriggerType::put);

        // 2. Top 5 địa điểm được nghe nhiều nhất
        List<DashboardResponse.TopLocationStat> topLocations = logs.stream().filter(l -> l.getLocation() != null)
                .collect(Collectors.groupingBy(PlaybackLog::getLocation, Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<vn.edu.sgu.audiotour.entity.Location, Long>comparingByValue().reversed()).limit(5)
                .map(e -> new DashboardResponse.TopLocationStat(e.getKey().getId(), e.getKey().getName(), e.getValue()))
                .collect(Collectors.toList());

        // 3. Danh sách tọa độ Heatmap
        List<DashboardResponse.HeatmapPoint> heatmapPoints = logs.stream().filter(l -> l.getLatitude() != null && l.getLongitude() != null)
                .collect(Collectors.groupingBy(l -> Math.round(l.getLatitude() * 10000) + ":" + Math.round(l.getLongitude() * 10000), Collectors.counting())).entrySet().stream()
                .map(e -> { String[] p = e.getKey().split(":"); return new DashboardResponse.HeatmapPoint(Long.parseLong(p[0]) / 10000.0, Long.parseLong(p[1]) / 10000.0, e.getValue()); })
                .collect(Collectors.toList());

        // 4. Danh sách thiết bị ĐANG ONLINE thời gian thực
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<DashboardResponse.ActiveUserSessionDTO> activeList = liveAudience.snapshot().stream()
                .map(s -> DashboardResponse.ActiveUserSessionDTO.builder()
                        .deviceId(s.deviceId())
                        .locationId(s.locationId())
                        .locationName(s.locationName())
                        .languageCode(s.languageCode())
                        .connectedDurationSeconds(s.connectedSeconds())
                        .listenDurationSeconds(s.listenSeconds())
                        .lastActiveTime(LocalDateTime.now().format(timeFmt))
                        .build())
                .collect(Collectors.toList());

        // 5. Danh sách 10 lượt nghe gần đây nhất phục vụ hiển thị lịch sử
        List<PlaybackLog> recentLogs = logs.stream().sorted(Comparator.comparing(PlaybackLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))).limit(10).toList();
        List<DashboardResponse.RecentLogDTO> recentList = recentLogs.stream()
                .map(l -> DashboardResponse.RecentLogDTO.builder()
                        .id(l.getId())
                        .deviceId(l.getDeviceId())
                        .locationName(l.getLocation() != null ? l.getLocation().getName() : "Không rõ")
                        .languageCode(l.getLanguageCode())
                        .durationSeconds(l.getListenDurationSeconds())
                        .triggerType(l.getTriggerType() != null ? l.getTriggerType().name() : "QR_CODE")
                        .completedAt(l.getCreatedAt() != null ? l.getCreatedAt().format(timeFmt) : "")
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalListens(totalListens)
                .averageListenDurationSeconds(Math.round(avgDuration * 10.0) / 10.0)
                .listensByTriggerType(listensByTriggerType)
                .topLocations(topLocations)
                .heatmapPoints(heatmapPoints)
                .activeUsers(activeList.size())
                .listeningUsers((int) activeList.stream().filter(s -> s.getLocationId() != null).count())
                .activeSessions(activeList)
                .recentLogs(recentList)
                .serverIp(serverAddress.detect())
                .uniqueDevices(logs.stream().map(PlaybackLog::getDeviceId).filter(Objects::nonNull).distinct().count())
                .offlineDownloads(packageDownloadRepository.count())
                .listensByDay(logs.stream().filter(l -> l.getCreatedAt() != null).collect(Collectors.groupingBy(l -> l.getCreatedAt().toLocalDate().toString(), TreeMap::new, Collectors.counting())).entrySet().stream().map(e -> new DashboardResponse.TimeSeriesPoint(e.getKey(), e.getValue())).toList())
                .listensByLanguage(logs.stream().filter(l -> l.getLanguageCode() != null && !l.getLanguageCode().isBlank()).collect(Collectors.groupingBy(PlaybackLog::getLanguageCode, TreeMap::new, Collectors.counting())))
                .build();
    }
}
