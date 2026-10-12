package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.audiotour.dto.DashboardResponse;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "3. Phân tích Dữ liệu & Dashboard (Analytics Controller)", description = "Các API ghi log nghe thuyết minh từ Mobile App và cung cấp số liệu Dashboard cho CMS (Top POI, Heatmap, thời gian nghe)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/logs")
    @Operation(summary = "Ghi log khi du khách nghe xong bài thuyết minh", description = "Mobile App gửi log sau khi phát audio (qua quét mã QR hoặc GPS geofencing) để ghi nhận lượt nghe và vị trí ẩn danh")
    public ResponseEntity<PlaybackLog> recordPlaybackLog(@RequestBody PlaybackLogRequest request) {
        return ResponseEntity.ok(analyticsService.recordPlaybackLog(request));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Lấy dữ liệu thống kê tổng quan (Dashboard CMS)", description = "Cung cấp tổng số lượt nghe, thời gian nghe trung bình, Top địa điểm nghe nhiều nhất, tỷ lệ QR vs GPS và các điểm tọa độ Heatmap")
    public ResponseEntity<DashboardResponse> getDashboardData() {
        return ResponseEntity.ok(analyticsService.getDashboardData());
    }
}
