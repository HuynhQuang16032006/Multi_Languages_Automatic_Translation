package vn.edu.sgu.audiotour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // Ví dụ: "Dinh Độc Lập", "Chợ Bến Thành"

    @Column(length = 255)
    private String address;

    private Double latitude; // Tọa độ GPS để hiển thị bản đồ
    private Double longitude;

    private String imageUrl; // Link ảnh đại diện địa điểm
}