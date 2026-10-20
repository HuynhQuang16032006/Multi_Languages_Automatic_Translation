package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.service.NarrationService;

@RestController
@RequestMapping("/api/narrations")
@RequiredArgsConstructor
@Tag(name = "6. Thuyết minh (Narration Controller)", description = "Các API lấy nội dung thuyết minh (văn bản + file âm thanh) theo địa điểm và ngôn ngữ phục vụ app du khách")
public class NarrationController {

    private final NarrationService narrationService;

    @GetMapping
    @Operation(summary = "Lấy bài thuyết minh theo địa điểm và ngôn ngữ", description = "Trả về nội dung văn bản thuyết minh và đường dẫn file âm thanh tương ứng với POI + mã ngôn ngữ (ví dụ: vi, en)")
    public ResponseEntity<NarrationResponse> getNarration(
            @Parameter(description = "ID của địa điểm (POI)", example = "5", required = true) @RequestParam Long locationId,
            @Parameter(description = "Mã ngôn ngữ (ví dụ: 'vi', 'en')", example = "vi", required = true) @RequestParam String languageCode) {
        return ResponseEntity.ok(narrationService.getNarration(locationId, languageCode));
    }
}
