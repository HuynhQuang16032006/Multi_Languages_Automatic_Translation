package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Nguồn duy nhất ghi Playback Log: chuẩn hoá thời lượng nghe, trigger, toạ độ và thời điểm.
 * Cả LiveAudience (kết thúc phiên) và POST /api/analytics/logs đều đi qua interface này.
 */
@Service
@RequiredArgsConstructor
public class PlaybackLogRecorder {

    // Log ngắn hơn 5 giây không có ý nghĩa thống kê
    private static final int MIN_LOGGED_SECONDS = 5;

    private final PlaybackLogRepository playbackLogRepository;
    private final LocationRepository locationRepository;
    private final Clock clock;

    @Transactional
    public PlaybackLog record(Entry entry) {
        Location location = locationRepository.findById(entry.locationId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + entry.locationId()));

        PlaybackLog log = new PlaybackLog();
        log.setLocation(location);
        log.setLanguageCode(entry.languageCode());
        log.setListenDurationSeconds(Math.max(entry.listenSeconds(), MIN_LOGGED_SECONDS));
        log.setTriggerType(entry.triggerType() != null ? entry.triggerType() : TriggerType.QR_CODE);
        log.setLatitude(entry.latitude() != null ? entry.latitude() : location.getLatitude());
        log.setLongitude(entry.longitude() != null ? entry.longitude() : location.getLongitude());
        log.setDeviceId(entry.deviceId());
        log.setCreatedAt(LocalDateTime.now(clock));
        return playbackLogRepository.save(log);
    }

    /** Toạ độ null = lấy theo POI (đường heartbeat không có GPS của thiết bị). */
    public record Entry(Long locationId, String languageCode, int listenSeconds,
                        TriggerType triggerType, String deviceId, Double latitude, Double longitude) {
    }
}
