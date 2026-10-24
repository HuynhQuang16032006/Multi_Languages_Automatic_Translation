package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.sgu.audiotour.config.LanguageProperties;
import vn.edu.sgu.audiotour.config.LanguageProperties.SupportedLanguage;
import vn.edu.sgu.audiotour.dto.AddLanguageResponse;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Thêm ngôn ngữ mới: dịch toàn bộ bản thuyết minh tiếng Việt sang ngôn ngữ đích,
 * lưu thành Narration mới, rồi sinh audio cho từng bản (ADR 0005).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageOnboardingService {

    private static final String SOURCE_LANGUAGE = "vi";

    private final LanguageProperties languageProperties;
    private final LanguageRepository languageRepository;
    private final NarrationRepository narrationRepository;
    private final LanguageService languageService;
    private final TranslationService translationService;
    private final TtsService ttsService;

    public AddLanguageResponse add(String code) {
        SupportedLanguage supported = findSupported(code)
                .orElseThrow(() -> new IllegalArgumentException("Ngôn ngữ không được hỗ trợ: " + code));
        String normalizedCode = supported.code();

        // Pre-check trước khi gọi DeepL: trùng thì không tốn credit dịch
        if (languageRepository.findByCode(normalizedCode).isPresent()) {
            throw new ConflictException("Mã ngôn ngữ đã tồn tại: " + normalizedCode);
        }

        List<Narration> sources = narrationRepository.findAllByLanguageCode(SOURCE_LANGUAGE);
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("Chưa có bản thuyết minh tiếng Việt để dịch");
        }

        // Dịch xong hết mới ghi: lỗi ở bất kỳ POI nào thì DB không đổi
        List<Narration> narrations = new ArrayList<>();
        for (Narration source : sources) {
            Narration narration = new Narration();
            narration.setLocation(source.getLocation());
            narration.setTextContent(translationService.translate(source.getTextContent(), normalizedCode));
            narrations.add(narration);
        }

        Language language = languageService.saveWithNarrations(
                new Language(null, normalizedCode, supported.name()), narrations);

        // Audio sinh sau khi commit, lỗi từng file không làm hỏng cả ngôn ngữ
        int audioGenerated = 0;
        for (Narration narration : narrations) {
            try {
                ttsService.generateAudio(narration.getId(), false);
                audioGenerated++;
            } catch (RuntimeException ex) {
                log.warn("Không sinh được audio cho narration {} ({}): {}",
                        narration.getId(), normalizedCode, ex.getMessage());
            }
        }

        return new AddLanguageResponse(language, narrations.size(), audioGenerated);
    }

    private Optional<SupportedLanguage> findSupported(String code) {
        List<SupportedLanguage> supported = languageProperties.supported();
        if (supported == null) {
            return Optional.empty();
        }
        return supported.stream()
                .filter(item -> item.code().equalsIgnoreCase(code))
                .findFirst();
    }
}
