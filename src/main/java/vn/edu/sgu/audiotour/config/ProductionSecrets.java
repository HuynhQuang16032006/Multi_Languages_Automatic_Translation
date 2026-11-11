package vn.edu.sgu.audiotour.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProductionSecrets {
    public ProductionSecrets(@Value("${jwt.secret}") String secret,
                             @Value("${app.admin.password}") String adminPassword,
                             @Value("${spring.datasource.password}") String databasePassword) {
        if (secret.isBlank() || secret.equals("dev-secret-key-change-me-in-production-0123456789abcdef")
                || adminPassword.isBlank() || adminPassword.equals("admin123")
                || databasePassword.isBlank() || databasePassword.equals("123456")) {
            throw new IllegalStateException("Production không được dùng secret hoặc mật khẩu mặc định của môi trường phát triển");
        }
    }
}
