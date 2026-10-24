package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.audiotour.dto.LanguageRequest;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.service.LanguageService;
import vn.edu.sgu.audiotour.config.LanguageProperties;
import vn.edu.sgu.audiotour.config.LanguageProperties.SupportedLanguage;
import vn.edu.sgu.audiotour.dto.AddLanguageResponse;
import vn.edu.sgu.audiotour.service.LanguageOnboardingService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "5. Quản lý Ngôn ngữ (Language Controller)", description = "Các API CRUD quản lý danh mục ngôn ngữ hỗ trợ thuyết minh đa ngôn ngữ của du khách")
public class LanguageController {

    private final LanguageService languageService;
    private final LanguageOnboardingService languageOnboardingService;
    private final LanguageProperties languageProperties;

    @GetMapping("/languages")
    @Operation(summary = "Lấy danh sách ngôn ngữ (dạng mảng phẳng)", description = "Trả về toàn bộ ngôn ngữ đang được hỗ trợ, giữ nguyên định dạng mảng JSON cho client cũ")
    public ResponseEntity<List<Language>> getAllLanguages() {
        return ResponseEntity.ok(languageService.getAllLanguages());
    }

    // Ngôn ngữ admin được phép thêm, đọc từ cấu hình: giao của TTS và DeepL (ADR 0005)
    @GetMapping("/languages/supported")
    @Operation(summary = "Lấy danh sách ngôn ngữ có thể thêm", description = "Trả về các mã ngôn ngữ hỗ trợ cả dịch máy và TTS, đọc từ cấu hình app.languages.supported")
    public ResponseEntity<List<SupportedLanguage>> getSupportedLanguages() {
        List<SupportedLanguage> supported = languageProperties.supported();
        return ResponseEntity.ok(supported == null ? List.of() : supported);
    }

    @GetMapping("/v1/languages")
    @Operation(summary = "Lấy danh sách ngôn ngữ có phân trang", description = "Trả về danh sách ngôn ngữ dạng Page (page, size, sort) cho phiên bản API v1")
    public ResponseEntity<Page<Language>> getLanguages(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(languageService.getLanguages(pageable));
    }

    @GetMapping({"/languages/{id}", "/v1/languages/{id}"})
    @Operation(summary = "Lấy chi tiết 1 ngôn ngữ theo ID", description = "Truy xuất thông tin chi tiết của một ngôn ngữ")
    public ResponseEntity<Language> getLanguageById(
            @Parameter(description = "ID của ngôn ngữ", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(languageService.getLanguageById(id));
    }

    @PostMapping({"/languages", "/v1/languages"})
    @Operation(summary = "Thêm mới 1 ngôn ngữ", description = "Tạo mới ngôn ngữ, trả về 409 Conflict nếu mã code đã tồn tại")
    public ResponseEntity<Language> createLanguage(@Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.createLanguage(request));
    }

    // Dịch toàn bộ bản thuyết minh tiếng Việt sang ngôn ngữ đích rồi sinh audio (ADR 0005)
    @PostMapping("/languages/{code}/translate")
    @Operation(summary = "Thêm ngôn ngữ mới bằng dịch máy + TTS",
            description = "Dịch toàn bộ narration tiếng Việt sang mã ngôn ngữ trong danh sách hỗ trợ, lưu thành narration mới, sinh audio ngay. "
                    + "Trả 400 nếu mã không được hỗ trợ, 409 nếu ngôn ngữ đã tồn tại, 503 nếu thiếu key DeepL hoặc DeepL lỗi.")
    public ResponseEntity<AddLanguageResponse> addLanguageWithTranslation(
            @Parameter(description = "Mã ngôn ngữ cần thêm", example = "ja") @PathVariable String code) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageOnboardingService.add(code));
    }

    @PutMapping({"/languages/{id}", "/v1/languages/{id}"})
    @Operation(summary = "Cập nhật 1 ngôn ngữ theo ID", description = "Cập nhật mã code và tên ngôn ngữ")
    public ResponseEntity<Language> updateLanguage(
            @Parameter(description = "ID của ngôn ngữ", example = "1") @PathVariable Long id,
            @Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.ok(languageService.updateLanguage(id, request));
    }

    @DeleteMapping({"/languages/{id}", "/v1/languages/{id}"})
    @Operation(summary = "Xóa 1 ngôn ngữ theo ID", description = "Xóa ngôn ngữ khỏi hệ thống, trả về 204 No Content")
    public ResponseEntity<Void> deleteLanguage(
            @Parameter(description = "ID của ngôn ngữ", example = "1") @PathVariable Long id) {
        languageService.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }
}
