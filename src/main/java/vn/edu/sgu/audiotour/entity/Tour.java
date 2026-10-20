package vn.edu.sgu.audiotour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tours")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(description = "Thực thể Tuyến tham quan (Tour) liên kết nhiều địa điểm POI")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID của Tour", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Tên tuyến tham quan", example = "Tour Di tích Lịch sử Quận 4 & Dinh Độc Lập")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Mô tả lộ trình tour", example = "Khám phá các trạm di tích lịch sử và văn hóa nổi tiếng")
    private String description;

    @Schema(description = "Thời lượng ước tính (phút)", example = "105")
    private Integer estimatedDurationMinutes;

    @Schema(description = "Ảnh đại diện tour", example = "https://example.com/images/tour-quan-4.jpg")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "tour_locations",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    @JsonIgnoreProperties({"subLocations"})
    @Schema(description = "Danh sách các POI thuộc tuyến tour")
    private List<Location> locations = new ArrayList<>();
}
