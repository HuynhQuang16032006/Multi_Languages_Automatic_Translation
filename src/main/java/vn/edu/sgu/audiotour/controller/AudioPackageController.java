package vn.edu.sgu.audiotour.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.audiotour.service.AudioPackageService;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class AudioPackageController {

    private final AudioPackageService audioPackageService;

    // API tải file nén phục vụ quét QR offline
    // Cú pháp gọi: /api/packages/offline?locationId=1&languageCode=vi
    @GetMapping("/offline")
    public ResponseEntity<byte[]> downloadOfflinePackage(
            @RequestParam Long locationId,
            @RequestParam String languageCode) {

        byte[] zipData = audioPackageService.createOfflinePackage(locationId, languageCode);

        return ResponseEntity.ok()
                // Yêu cầu trình duyệt/app tự động tải file xuống với tên tour-offline.zip
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tour-offline.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipData);
    }
}