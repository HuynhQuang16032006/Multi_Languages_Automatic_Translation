package vn.edu.sgu.audiotour.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "locations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(description = "Thực thể Điểm quan tâm (POI / Location) hỗ trợ cấu trúc Cha-Con, GPS Geofencing và QR Code")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Mã định danh địa điểm", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Tên địa điểm / POI", example = "Dinh Độc Lập")
    private String name;

    @Column(length = 255)
    @Schema(description = "Địa chỉ cụ thể", example = "135 Nam Kỳ Khởi Nghĩa, Bến Thành, Quận 1, TP.HCM")
    private String address;

    @Schema(description = "Tọa độ GPS Vĩ độ (Latitude)", example = "10.7770")
    private Double latitude;

    @Schema(description = "Tọa độ GPS Kinh độ (Longitude)", example = "106.6953")
    private Double longitude;

    @Column(name = "radius_in_meters")
    @Schema(description = "Bán kính Geofence (mét) để kích hoạt thuyết minh khi tới gần", example = "30.0")
    private Double radiusInMeters = 30.0;

    @Schema(description = "Độ ưu tiên phát của POI (số càng lớn ưu tiên càng cao)", example = "1")
    private Integer priority = 1;

    @Column(name = "qr_code", unique = true, length = 50)
    @Schema(description = "Mã QR gắn tại điểm tham quan / trạm dừng", example = "QR-KHANH-HOI-01")
    private String qrCode;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://example.com/images/dinh-doc-lap.jpg")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Schema(description = "Địa điểm cha (nếu là phòng ban/khu vực con)")
    private Location parentLocation;

    @OneToMany(mappedBy = "parentLocation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"parentLocation", "subLocations"})
    @Schema(description = "Danh sách các khu vực con")
    private List<Location> subLocations = new ArrayList<>();
}