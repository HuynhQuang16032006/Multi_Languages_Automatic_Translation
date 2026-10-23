package vn.edu.sgu.audiotour.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Cấu hình nhà cung cấp TTS. Một voice dùng chung cho mọi ngôn ngữ (ElevenLabs voice không gắn ngôn ngữ).
@ConfigurationProperties(prefix = "app.tts")
public record TtsProperties(String apiKey, String baseUrl, String modelId, String voiceId) {
}
