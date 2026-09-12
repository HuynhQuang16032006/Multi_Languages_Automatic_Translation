package vn.edu.sgu.audiotour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "languages")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code; // Ví dụ: "vi", "en", "fr"

    @Column(nullable = false, length = 50)
    private String name; // Ví dụ: "Tiếng Việt", "English"
}