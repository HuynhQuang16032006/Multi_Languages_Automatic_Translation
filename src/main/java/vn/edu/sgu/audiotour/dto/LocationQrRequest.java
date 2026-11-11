package vn.edu.sgu.audiotour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LocationQrRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,50}") String qrCode,
        String expectedQrCode) {
}
