package vn.edu.sgu.audiotour.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "narrations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Narration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(columnDefinition = "TEXT")
    private String textContent; // Nội dung văn bản thuyết minh

    @Column(length = 255)
    private String audioUrl; // Chứa tên file MP3, ví dụ "phong-khanh-tiet-vi.mp3"

    private Integer durationInSeconds; // Độ dài file âm thanh tính theo giây
}