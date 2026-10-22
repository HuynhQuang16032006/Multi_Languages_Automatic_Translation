package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi sau khi ghi nhận nhịp tim (Heartbeat) của thiết bị")
public class HeartbeatResponse {

    @Schema(description = "Trạng thái phiên nghe sau khi xử lý", example = "ACTIVE")
    private Status status;

    @Schema(description = "Mã thiết bị, sinh mới nếu request không gửi", example = "iPhone-1234")
    private String deviceId;

    @Schema(description = "Số thiết bị đang online sau khi xử lý", example = "4")
    private Integer activeUsers;

    public enum Status {
        ACTIVE, LEFT
    }
}
