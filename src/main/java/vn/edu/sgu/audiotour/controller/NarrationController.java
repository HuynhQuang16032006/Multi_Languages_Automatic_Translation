package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.service.NarrationService;
import vn.edu.sgu.audiotour.service.TtsService;

@RestController
@RequestMapping("/api/narrations")
@RequiredArgsConstructor
@Tag(name = "6. Thuyết minh (Narration Controller)", description = "Các API lấy nội dung thuyết minh (văn bản + file âm thanh) theo địa điểm và ngôn ngữ phục vụ app du khách")
public class NarrationController {

    private final NarrationService narrationService;
    private final TtsService ttsService;

    @GetMapping
    @Operation(summary = "Lấy bài thuyết minh theo địa điểm và ngôn ngữ", description = "Trả về nội dung văn bản thuyết minh và đường dẫn file âm thanh tương ứng với POI + mã ngôn ngữ (ví dụ: vi, en)")
    public ResponseEntity<NarrationResponse> getNarration(
            @Parameter(description = "ID của địa điểm (POI)", example = "5", required = true) @RequestParam Long locationId,
            @Parameter(description = "Mã ngôn ngữ (ví dụ: 'vi', 'en')", example = "vi", required = true) @RequestParam String languageCode) {
        return ResponseEntity.ok(narrationService.getNarration(locationId, languageCode));
    }

    @PostMapping("/{id}/audio")
    @Operation(summary = "Sinh file MP3 thuyết minh bằng TTS", description = "Đọc textContent của bài thuyết minh bằng ElevenLabs, ghi file vào uploads/audio và lưu tên file vào audioUrl. File đã tồn tại thì dùng lại; gửi force=true để sinh lại.")
    public ResponseEntity<NarrationResponse> generateAudio(
            @Parameter(description = "ID của bài thuyết minh", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "true = sinh lại dù file đã tồn tại (tốn credit)", example = "false") @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(ttsService.generateAudio(id, force));
    }
}
