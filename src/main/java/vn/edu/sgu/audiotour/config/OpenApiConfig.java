package vn.edu.sgu.audiotour.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Multi Languages Automatic Translation - Audio Tour API")
                        .description("Hệ thống Thuyết minh Audio Tour tự động đa ngôn ngữ\n"
                                + "- **Hỗ trợ chế độ Hybrid:** Quét mã QR tại điểm dừng / di tích (tải toàn bộ file MP3 và text offline dạng ZIP) & Định vị GPS Geofencing ngoài trời.\n"
                                + "- **Phân tích dữ liệu:** Ghi nhận lịch sử nghe ẩn danh, phân tích Top POI và Heatmap vị trí du khách.\n"
                                + "- **Đồ án Công nghệ Phần mềm** - Trường Đại học Sài Gòn (SGU).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Khoa Công nghệ Thông tin - Trường ĐH Sài Gòn (SGU)")
                                .email("contact@sgu.edu.vn"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
