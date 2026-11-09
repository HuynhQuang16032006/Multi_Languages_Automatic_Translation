package vn.edu.sgu.audiotour.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 255)
    private String address;
    @DecimalMin("-90") @DecimalMax("90")
    private Double latitude;
    @DecimalMin("-180") @DecimalMax("180")
    private Double longitude;
    @Positive
    private Double radiusInMeters;
    @PositiveOrZero
    private Integer priority;
    @Size(max = 50)
    private String qrCode;
    @Size(max = 255)
    private String imageUrl;
    @Positive
    private Long parentId;
}
