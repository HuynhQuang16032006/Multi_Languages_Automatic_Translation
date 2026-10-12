package vn.edu.sgu.audiotour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "playback_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(description = "Thực thể ghi log lịch sử nghe thuyết minh (phục vụ thống kê, heatmap, phân tích lộ trình ẩn danh)")
public class PlaybackLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "language_code", length = 10, nullable = false)
    @Schema(description = "Mã ngôn ngữ du khách nghe", example = "vi")
    private String languageCode;

    @Column(name = "listen_duration_seconds")
    @Schema(description = "Thời gian thực tế du khách đã nghe (giây)", example = "45")
    private Integer listenDurationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", length = 20, nullable = false)
    @Schema(description = "Phương thức kích hoạt (QR_CODE, GEOFENCE, MANUAL)", example = "QR_CODE")
    private TriggerType triggerType;

    @Schema(description = "Vĩ độ của du khách khi bắt đầu nghe (phục vụ heatmap)", example = "10.7770")
    private Double latitude;

    @Schema(description = "Kinh độ của du khách khi bắt đầu nghe (phục vụ heatmap)", example = "106.6953")
    private Double longitude;

    @Column(name = "device_id", length = 64)
    @Schema(description = "Mã định danh thiết bị ẩn danh", example = "anon-dev-abc123xyz")
    private String deviceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
