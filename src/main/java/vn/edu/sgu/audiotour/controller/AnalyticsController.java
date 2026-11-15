package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.dto.DeviceHeartbeatRequest;
import vn.edu.sgu.audiotour.dto.HeartbeatResponse;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.service.AnalyticsService;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "3. Phân tích Dữ liệu & Dashboard (Analytics Controller)", description = "Các API ghi log nghe thuyết minh từ Mobile App và cung cấp số liệu Dashboard cho CMS (Top POI, Heatmap, thời gian nghe)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final LiveAudience liveAudience;
    private final PlaybackLogRecorder playbackLogRecorder;
    private final LocationService locationService;

    @PostMapping("/heartbeat")
    @Operation(summary = "Gửi nhịp tim (Heartbeat) giám sát thiết bị truy cập thời gian thực", description = "Mobile App gửi định kỳ mỗi 3s (action=JOIN/PING/LEAVE). Khi thiết bị out hoặc ngắt kết nối quá 8s, số lượng online tự động giảm và lưu lịch sử")
    public ResponseEntity<HeartbeatResponse> recordHeartbeat(@Valid @RequestBody DeviceHeartbeatRequest request) {
        LiveAudience.HeartbeatResult result = liveAudience.record(request);
        return ResponseEntity.ok(new HeartbeatResponse(result.status(), result.deviceId(), result.activeUsers()));
    }

    @PostMapping("/logs")
    @Operation(summary = "Ghi log khi du khách nghe xong bài thuyết minh", description = "Mobile App gửi log sau khi phát audio (qua quét mã QR hoặc GPS geofencing) để ghi nhận lượt nghe và vị trí ẩn danh")
    public ResponseEntity<PlaybackLog> recordPlaybackLog(@Valid @RequestBody PlaybackLogRequest request) {
        locationService.getLocationById(request.getLocationId());
        PlaybackLogRecorder.Entry entry = new PlaybackLogRecorder.Entry(
                request.getLocationId(), request.getLanguageCode(), request.getListenDurationSeconds(),
                request.getTriggerType(), request.getDeviceId(), request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(playbackLogRecorder.record(entry));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Lấy dữ liệu thống kê tổng quan (Dashboard CMS)", description = "Cung cấp tổng số lượt nghe, số lượng online thời gian thực, Top địa điểm nghe nhiều nhất, tỷ lệ QR vs GPS và các điểm tọa độ Heatmap")
    public ResponseEntity<DashboardResponse> getDashboardData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String languageCode) {
        return ResponseEntity.ok(analyticsService.getDashboardData(from, to, languageCode));
    }
}
