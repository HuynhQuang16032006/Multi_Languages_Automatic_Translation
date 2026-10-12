package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vn.edu.sgu.audiotour.entity.TriggerType;

@Data
@Schema(description = "Request gửi log phát thuyết minh từ Mobile App")
public class PlaybackLogRequest {

    @Schema(description = "ID của địa điểm (POI)", example = "1")
    private Long locationId;

    @Schema(description = "Mã ngôn ngữ", example = "vi")
    private String languageCode;

    @Schema(description = "Thời gian thực tế người dùng đã nghe (giây)", example = "65")
    private Integer listenDurationSeconds;

    @Schema(description = "Cách thức kích hoạt (QR_CODE hoặc GEOFENCE hoặc MANUAL)", example = "QR_CODE")
    private TriggerType triggerType = TriggerType.QR_CODE;

    @Schema(description = "Tọa độ GPS Latitude của thiết bị lúc nghe", example = "10.7770")
    private Double latitude;

    @Schema(description = "Tọa độ GPS Longitude của thiết bị lúc nghe", example = "106.6953")
    private Double longitude;

    @Schema(description = "Mã thiết bị ẩn danh (UUID tạo trên mobile app)", example = "anon-device-12345")
    private String deviceId;
}
