package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.sgu.audiotour.dto.DeviceHeartbeatRequest;
import vn.edu.sgu.audiotour.dto.HeartbeatResponse;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Theo dõi các phiên nghe đang mở và ghi playback log khi phiên kết thúc.
 * Đây là nguồn duy nhất của con số "đang truy cập" trên dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveAudience {

    // Quá 8 giây không có heartbeat thì coi như thiết bị đã rời đi
    private static final long HEARTBEAT_TIMEOUT_MILLIS = 8_000;
    // Log ngắn hơn 5 giây không có ý nghĩa thống kê (giữ hành vi cũ)
    private static final int MIN_LOGGED_SECONDS = 5;

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final LocationRepository locationRepository;
    private final PlaybackLogRepository playbackLogRepository;
    private final Clock clock;

    /**
     * Ghi nhận một heartbeat. Ném IllegalArgumentException (400) khi request thiếu
     * locationId/languageCode hoặc action lạ, NotFoundException (404) khi POI không tồn tại.
     */
    public HeartbeatResult record(DeviceHeartbeatRequest request) {
        long now = clock.millis();
        String action = action(request.getAction());
        String deviceId = deviceId(request.getDeviceId());

        // Dọn trước để con số trả về là số đang thật sự online
        reapExpired(now);

        if ("LEAVE".equals(action)) {
            return leave(deviceId, request);
        }
        return joinOrPing(deviceId, request, now);
    }

    /** Ảnh chụp các phiên đang online; tự dọn phiên hết hạn trước khi trả. */
    public List<LiveSession> snapshot() {
        long now = clock.millis();
        reapExpired(now);
        return sessions.values().stream()
                .map(s -> new LiveSession(s.deviceId, s.locationId, s.locationName, s.languageCode,
                        (now - s.joinedAt) / 1000, s.listenSeconds))
                .toList();
    }

    private HeartbeatResult joinOrPing(String deviceId, DeviceHeartbeatRequest request, long now) {
        Long locationId = requireLocationId(request);
        String languageCode = requireLanguage(request);

        Session session = sessions.get(deviceId);
        boolean joined = session == null;
        boolean locationChanged = joined || !locationId.equals(session.locationId);
        // Chỉ tra DB khi phiên mới hoặc đổi POI: PING 3 giây/lần không cần query
        String locationName = locationChanged ? lookupLocationName(locationId) : session.locationName;
        TriggerType triggerType = request.getTriggerType() != null ? request.getTriggerType() : TriggerType.QR_CODE;

        int listenSeconds = request.getListenDurationSeconds() != null
                ? Math.max(0, request.getListenDurationSeconds())
                : 0;

        if (joined) {
            sessions.put(deviceId,
                    new Session(deviceId, locationId, locationName, languageCode, triggerType, now, listenSeconds));
        } else {
            session.lastHeartbeatAt = now;
            session.locationId = locationId;
            session.locationName = locationName;
            session.languageCode = languageCode;
            session.triggerType = triggerType;
            session.listenSeconds = Math.max(session.listenSeconds, listenSeconds);
        }

        return new HeartbeatResult(HeartbeatResponse.Status.ACTIVE, deviceId, sessions.size());
    }

    /** LEAVE không thấy phiên nghĩa là phiên đã được dọn trước đó -> không ghi log lần hai. */
    private HeartbeatResult leave(String deviceId, DeviceHeartbeatRequest request) {
        Session removed = sessions.remove(deviceId);
        if (removed != null) {
            int duration = request.getListenDurationSeconds() != null
                    ? Math.max(removed.listenSeconds, request.getListenDurationSeconds())
                    : removed.listenSeconds;
            String languageCode = request.getLanguageCode() != null && !request.getLanguageCode().isBlank()
                    ? request.getLanguageCode()
                    : removed.languageCode;
            writeLog(removed.locationId, languageCode, duration, deviceId, removed.triggerType);
        }
        return new HeartbeatResult(HeartbeatResponse.Status.LEFT, deviceId, sessions.size());
    }

    private void reapExpired(long now) {
        sessions.forEach((deviceId, session) -> {
            if (now - session.lastHeartbeatAt <= HEARTBEAT_TIMEOUT_MILLIS) {
                return;
            }
            // remove(key, value): nếu LEAVE vừa xoá phiên này thì không ghi log thêm lần nữa
            if (!sessions.remove(deviceId, session)) {
                return;
            }
            if (session.listenSeconds > 0) {
                writeLog(session.locationId, session.languageCode, session.listenSeconds, deviceId, session.triggerType);
            }
        });
    }

    private void writeLog(Long locationId, String languageCode, int listenSeconds, String deviceId,
                          TriggerType triggerType) {
        try {
            Location location = locationRepository.findById(locationId).orElse(null);
            if (location == null) {
                return;
            }
            PlaybackLog entry = new PlaybackLog();
            entry.setLocation(location);
            entry.setLanguageCode(languageCode);
            entry.setListenDurationSeconds(Math.max(listenSeconds, MIN_LOGGED_SECONDS));
            entry.setTriggerType(triggerType != null ? triggerType : TriggerType.QR_CODE);
            entry.setLatitude(location.getLatitude());
            entry.setLongitude(location.getLongitude());
            entry.setDeviceId(deviceId);
            entry.setCreatedAt(LocalDateTime.now());
            playbackLogRepository.save(entry);
        } catch (RuntimeException e) {
            // Mất một log còn hơn để phiên sống lại hoặc số đếm sai
            log.warn("Không ghi được playback log cho thiết bị {}: {}", deviceId, e.getMessage());
        }
    }

    private String lookupLocationName(Long locationId) {
        return locationRepository.findById(locationId)
                .map(Location::getName)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + locationId));
    }

    private static String deviceId(String requested) {
        return requested != null && !requested.isBlank()
                ? requested
                : "dev-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String action(String requested) {
        if (requested == null || requested.isBlank()) {
            return "PING";
        }
        String value = requested.trim().toUpperCase(Locale.ROOT);
        if (!value.equals("JOIN") && !value.equals("PING") && !value.equals("LEAVE")) {
            throw new IllegalArgumentException(
                    "Action không hợp lệ: '" + requested + "'. Chỉ nhận JOIN, PING hoặc LEAVE.");
        }
        return value;
    }

    private static Long requireLocationId(DeviceHeartbeatRequest request) {
        if (request.getLocationId() == null) {
            throw new IllegalArgumentException("Thiếu locationId: heartbeat phải gắn với một địa điểm (POI).");
        }
        return request.getLocationId();
    }

    private static String requireLanguage(DeviceHeartbeatRequest request) {
        if (request.getLanguageCode() == null || request.getLanguageCode().isBlank()) {
            throw new IllegalArgumentException("Thiếu languageCode: không xác định được ngôn ngữ của phiên nghe.");
        }
        return request.getLanguageCode();
    }

    public record HeartbeatResult(HeartbeatResponse.Status status, String deviceId, int activeUsers) {
    }

    public record LiveSession(String deviceId, Long locationId, String locationName, String languageCode,
                              long connectedSeconds, int listenSeconds) {
    }

    private static final class Session {
        private final String deviceId;
        private final long joinedAt;
        private Long locationId;
        private String locationName;
        private String languageCode;
        private TriggerType triggerType;
        private long lastHeartbeatAt;
        private int listenSeconds;

        private Session(String deviceId, Long locationId, String locationName, String languageCode,
                        TriggerType triggerType, long joinedAt, int listenSeconds) {
            this.deviceId = deviceId;
            this.locationId = locationId;
            this.locationName = locationName;
            this.languageCode = languageCode;
            this.triggerType = triggerType;
            this.joinedAt = joinedAt;
            this.lastHeartbeatAt = joinedAt;
            this.listenSeconds = listenSeconds;
        }
    }
}
