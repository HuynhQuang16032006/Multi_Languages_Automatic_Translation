package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.service.AudioPackageService;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Tag(name = "2. Gói âm thanh Offline (Audio Package Controller)", description = "API đóng gói file ZIP chứa textContent và file âm thanh MP3 thật phục vụ nghe Offline khi không có 4G/Wifi")
public class AudioPackageController {

    private final AudioPackageService audioPackageService;

    @GetMapping("/offline")
    @Operation(
            summary = "Tải gói thuyết minh Offline dạng file ZIP",
            description = "Trả về file nén tour-offline.zip gồm content.txt (UTF-8) và file âm thanh thực tế theo ngôn ngữ người dùng chọn"
    )
    public ResponseEntity<byte[]> downloadOfflinePackage(
            @Parameter(description = "ID của địa điểm (POI)", example = "1") @RequestParam Long locationId,
            @Parameter(description = "Mã ngôn ngữ (ví dụ: 'vi', 'en')", example = "vi") @RequestParam String languageCode) {

        byte[] zipData = audioPackageService.createOfflinePackage(locationId, languageCode);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tour-offline.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipData);
    }
}