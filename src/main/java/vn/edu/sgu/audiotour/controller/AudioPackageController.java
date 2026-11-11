package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.NarrationService;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Tag(name = "2. Gói âm thanh & Phát trực tiếp (Audio Package Controller)", description = "API phát trực tiếp âm thanh thuyết minh (Streaming MP3) và đóng gói ZIP phục vụ nghe Offline")
public class AudioPackageController {

    private final NarrationService narrationService;
    private final LocationService locationService;

    @GetMapping(value = "/stream", produces = "audio/mpeg")
    @Operation(
            summary = "Phát trực tiếp âm thanh thuyết minh (Audio Streaming)",
            description = "Trả về luồng âm thanh audio/mpeg để trình duyệt hoặc Swagger UI tự động phát file âm thanh trực tiếp qua loa"
    )
    public ResponseEntity<Resource> streamAudio(
            @Parameter(description = "ID của địa điểm (POI)", example = "4") @RequestParam Long locationId,
            @Parameter(description = "Mã ngôn ngữ (ví dụ: 'vi', 'en')", example = "vi") @RequestParam String languageCode) {

        locationService.getLocationById(locationId);
        Resource audioResource = narrationService.getAudioResource(locationId, languageCode);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + audioResource.getFilename() + "\"")
                .body(audioResource);
    }

    @GetMapping("/offline")
    @Operation(
            summary = "Tải gói thuyết minh Offline dạng file ZIP",
            description = "Trả về file nén tour-offline.zip gồm content.txt (UTF-8) và file âm thanh thực tế theo ngôn ngữ người dùng chọn"
    )
    public ResponseEntity<byte[]> downloadOfflinePackage(
            @Parameter(description = "ID của địa điểm (POI)", example = "1") @RequestParam Long locationId,
            @Parameter(description = "Mã ngôn ngữ (ví dụ: 'vi', 'en')", example = "vi") @RequestParam String languageCode,
            @RequestParam(required = false) String deviceId) {

        locationService.getLocationById(locationId);
        byte[] zipData = narrationService.createOfflinePackage(locationId, languageCode, deviceId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tour-offline.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipData);
    }
}
