package vn.edu.sgu.audiotour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "narrations")
public class Narration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(columnDefinition = "TEXT")
    private String textContent; // Nội dung văn bản thuyết minh

    private String audioUrl; // Đường dẫn tới file âm thanh (MP3) trên server
    
    private Integer durationInSeconds; // Độ dài file âm thanh
}