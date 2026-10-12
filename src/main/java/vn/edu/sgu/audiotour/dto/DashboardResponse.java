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
}
