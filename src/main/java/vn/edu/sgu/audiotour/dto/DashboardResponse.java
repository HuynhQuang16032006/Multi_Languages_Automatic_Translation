package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dữ liệu thống kê tổng quan (Dashboard) cho hệ thống CMS")
public class DashboardResponse {

    @Schema(description = "Tổng số lượt nghe toàn hệ thống", example = "1250")
    private Long totalListens;

    @Schema(description = "Thời gian nghe trung bình 1 POI (giây)", example = "78.5")
    private Double averageListenDurationSeconds;

    @Schema(description = "Thống kê số lượt nghe theo phương thức kích hoạt (QR_CODE vs GEOFENCE)")
    private Map<String, Long> listensByTriggerType;

    @Schema(description = "Top các địa điểm được du khách nghe nhiều nhất")
    private List<TopLocationStat> topLocations;

    @Schema(description = "Danh sách các điểm tọa độ phục vụ vẽ bản đồ nhiệt (Heatmap)")
    private List<HeatmapPoint> heatmapPoints;

    @Schema(description = "Số lượng thiết bị đang truy cập / đang nghe theo thời gian thực", example = "4")
    private Integer activeUsers;

    @Schema(description = "Danh sách chi tiết các thiết bị đang online theo thời gian thực")
    private List<ActiveUserSessionDTO> activeSessions;

    @Schema(description = "Danh sách lịch sử các lượt nghe vừa hoàn thành gần đây")
    private List<RecentLogDTO> recentLogs;

    @Schema(description = "Địa chỉ IP LAN của máy chủ phục vụ thiết bị di động quét mã QR", example = "172.20.10.8")
    private String serverIp;

    private Long uniqueDevices;
    private Long offlineDownloads;
    private List<TimeSeriesPoint> listensByDay;
    private Map<String, Long> listensByLanguage;
    private Integer listeningUsers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSeriesPoint {
        private String date;
        private Long listenCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopLocationStat {
        private Long locationId;
        private String locationName;
        private Long listenCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HeatmapPoint {
        private Double latitude;
        private Double longitude;
        private Long intensity;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActiveUserSessionDTO {
        private String deviceId;
        private Long locationId;
        private String locationName;
        private String languageCode;
        private Long connectedDurationSeconds;
        private Integer listenDurationSeconds;
        private String lastActiveTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentLogDTO {
        private Long id;
        private String deviceId;
        private String locationName;
        private String languageCode;
        private Integer durationSeconds;
        private String triggerType;
        private String completedAt;
    }
}
