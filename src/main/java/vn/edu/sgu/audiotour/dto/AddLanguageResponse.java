package vn.edu.sgu.audiotour.dto;

import vn.edu.sgu.audiotour.entity.Language;

// Kết quả thêm ngôn ngữ: audioGenerated có thể nhỏ hơn translations nếu TTS lỗi giữa đường (ADR 0005)
public record AddLanguageResponse(Language language, int translations, int audioGenerated) {
}
