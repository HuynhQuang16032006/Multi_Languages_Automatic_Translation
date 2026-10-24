package vn.edu.sgu.audiotour.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// Danh sách ngôn ngữ admin được phép thêm: giao của ngôn ngữ TTS đọc được và ngôn ngữ DeepL dịch được.
@ConfigurationProperties(prefix = "app.languages")
public record LanguageProperties(List<SupportedLanguage> supported) {

    public record SupportedLanguage(String code, String name) {
    }
}
