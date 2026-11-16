package vn.edu.sgu.audiotour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "package_downloads")
@Getter
@NoArgsConstructor
public class PackageDownload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "language_code", length = 10, nullable = false)
    private String languageCode;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PackageDownload(Location location, String languageCode, String deviceId) {
        this.location = location;
        this.languageCode = languageCode;
        this.deviceId = deviceId;
    }
}
