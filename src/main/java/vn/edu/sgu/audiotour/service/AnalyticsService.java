package vn.edu.sgu.audiotour.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.dto.DeviceHeartbeatRequest;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PlaybackLogRepository playbackLogRepository;
    private final LocationRepository locationRepository;

    // Lưu trữ các phiên thiết bị đang online theo thời gian thực (Thread-safe)
    private final ConcurrentHashMap<String, ActiveSession> activeSessions = new ConcurrentHashMap<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveSession {
        private String deviceId;
        private Long locationId;
        private String locationName;
        private String languageCode;
        private long joinedAt;
        private long lastHeartbeatAt;
        private int listenSeconds;
    }

    /**
     * Xử lý tín hiệu nhịp tim (Heartbeat) từ thiết bị di động:
     * - JOIN: Thiết bị vừa quét QR kết nối vào
     * - PING: Thiết bị đang online duy trì kết nối
     * - LEAVE: Thiết bị thoát ra / dừng nghe -> Tự động giảm số lượng và ghi lại lịch sử
     */
    @Transactional
    public Map<String, Object> processHeartbeat(DeviceHeartbeatRequest request) {
        String deviceId = (request.getDeviceId() != null && !request.getDeviceId().isBlank())
                ? request.getDeviceId()
                : "dev-" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();
        String action = request.getAction() != null ? request.getAction().toUpperCase() : "PING";

        // Tự động dọn dẹp các thiết bị mất kết nối quá 8 giây (Heartbeat Timeout)
        cleanExpiredSessions(now);

        if ("LEAVE".equals(action)) {
            ActiveSession removed = activeSessions.remove(deviceId);
            int duration = request.getListenDurationSeconds() != null ? request.getListenDurationSeconds() : (removed != null ? removed.getListenSeconds() : 0);
            Long locId = request.getLocationId() != null ? request.getLocationId() : (removed != null ? removed.getLocationId() : 4L);
            String lang = request.getLanguageCode() != null ? request.getLanguageCode() : (removed != null ? removed.getLanguageCode() : "vi");

            if (duration > 0 || removed != null) {
                saveLogDirectly(locId, lang, duration, deviceId, TriggerType.QR_CODE);
            }

            Map<String, Object> res = new HashMap<>();
            res.put("status", "LEFT");
            res.put("deviceId", deviceId);
            res.put("activeUsers", activeSessions.size());
            return res;
        }

        // Xử lý JOIN hoặc PING
        String locName = "Dinh Độc Lập";
        if (request.getLocationId() != null) {
            Optional<Location> locOpt = locationRepository.findById(request.getLocationId());
            if (locOpt.isPresent()) {
                locName = locOpt.get().getName();
            }
        }

        ActiveSession session = activeSessions.get(deviceId);
        int listenSec = request.getListenDurationSeconds() != null ? request.getListenDurationSeconds() : 0;
        if (session == null) {
            session = ActiveSession.builder()
                    .deviceId(deviceId)
                    .locationId(request.getLocationId() != null ? request.getLocationId() : 4L)
                    .locationName(locName)
                    .languageCode(request.getLanguageCode() != null ? request.getLanguageCode() : "vi")
                    .joinedAt(now)
                    .lastHeartbeatAt(now)
                    .listenSeconds(listenSec)
                    .build();
            activeSessions.put(deviceId, session);
        } else {
            session.setLastHeartbeatAt(now);
            session.setListenSeconds(Math.max(session.getListenSeconds(), listenSec));
            if (request.getLocationId() != null) {
                session.setLocationId(request.getLocationId());
                session.setLocationName(locName);
            }
            if (request.getLanguageCode() != null) {
                session.setLanguageCode(request.getLanguageCode());
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("status", "ACTIVE");
        res.put("deviceId", deviceId);
        res.put("activeUsers", activeSessions.size());
        return res;
    }

    private void cleanExpiredSessions(long now) {
        activeSessions.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().getLastHeartbeatAt()) > 8000; // Quá 8s không ping coi như out
            if (expired && entry.getValue().getListenSeconds() > 0) {
                saveLogDirectly(entry.getValue().getLocationId(), entry.getValue().getLanguageCode(),
                        entry.getValue().getListenSeconds(), entry.getValue().getDeviceId(), TriggerType.QR_CODE);
            }
            return expired;
        });
    }

    private void saveLogDirectly(Long locationId, String lang, int duration, String deviceId, TriggerType triggerType) {
        try {
            Location loc = locationRepository.findById(locationId).orElse(null);
            if (loc != null) {
                PlaybackLog log = new PlaybackLog();
                log.setLocation(loc);
                log.setLanguageCode(lang);
                log.setListenDurationSeconds(duration > 0 ? duration : 5);
                log.setTriggerType(triggerType);
                log.setLatitude(loc.getLatitude());
                log.setLongitude(loc.getLongitude());
                log.setDeviceId(deviceId);
                log.setCreatedAt(LocalDateTime.now());
                playbackLogRepository.save(log);
            }
        } catch (Exception ignored) {
        }
    }

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

    @Transactional
    public DashboardResponse getDashboardData() {
        cleanExpiredSessions(System.currentTimeMillis());

        Long totalListens = playbackLogRepository.count();
        Double avgDuration = playbackLogRepository.getAverageListenDuration();
        if (avgDuration == null) {
            avgDuration = 0.0;
        }

        // 1. Thống kê theo phương thức kích hoạt
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

        // 4. Danh sách thiết bị ĐANG ONLINE thời gian thực
        long now = System.currentTimeMillis();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<DashboardResponse.ActiveUserSessionDTO> activeList = activeSessions.values().stream()
                .map(s -> DashboardResponse.ActiveUserSessionDTO.builder()
                        .deviceId(s.getDeviceId())
                        .locationId(s.getLocationId())
                        .locationName(s.getLocationName())
                        .languageCode(s.getLanguageCode())
                        .connectedDurationSeconds((now - s.getJoinedAt()) / 1000)
                        .listenDurationSeconds(s.getListenSeconds())
                        .lastActiveTime(LocalDateTime.now().format(timeFmt))
                        .build())
                .collect(Collectors.toList());

        // 5. Danh sách 10 lượt nghe gần đây nhất phục vụ hiển thị lịch sử
        List<PlaybackLog> recentLogs = playbackLogRepository.findTop10ByOrderByCreatedAtDesc();
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
                .activeUsers(activeSessions.size())
                .activeSessions(activeList)
                .recentLogs(recentList)
                .serverIp(detectServerIp())
                .build();
    }

    private String detectServerIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (!ip.startsWith("192.168.56.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "172.20.10.8";
    }
}
