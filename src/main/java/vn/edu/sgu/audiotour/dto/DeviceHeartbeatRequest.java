package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.sgu.audiotour.entity.TriggerType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request gửi nhịp tim (Heartbeat) giám sát người dùng đang truy cập theo thời gian thực")
public class DeviceHeartbeatRequest {

    @Size(max = 64)
    @Schema(description = "Mã định danh thiết bị duy nhất", example = "iPhone-14-Thầy")
    private String deviceId;

    @Schema(description = "ID của địa điểm (POI) đang xem hoặc đang nghe", example = "4")
    private Long locationId;

    @Schema(description = "Mã ngôn ngữ đang chọn", example = "vi")
    private String languageCode;

    @Schema(description = "Hành động: JOIN (vừa quét mã/kết nối), PING (duy trì nhịp tim), LEAVE (thoát ra / dừng nghe)", example = "PING")
    private String action; // JOIN, PING, LEAVE

    @Schema(description = "Số giây thực tế người dùng đã nghe", example = "25")
    private Integer listenDurationSeconds;

    @Schema(description = "Cách khách đến được POI: QR_CODE (quét QR), MANUAL (tự chọn), GEOFENCE. Mặc định QR_CODE.",
            example = "QR_CODE")
    private TriggerType triggerType;
}
