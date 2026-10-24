package vn.edu.sgu.audiotour.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Cấu hình dịch máy. apiKey rỗng thì app vẫn boot, endpoint thêm ngôn ngữ trả 503.
@ConfigurationProperties(prefix = "app.mt")
public record MtProperties(String apiKey, String baseUrl) {
}
