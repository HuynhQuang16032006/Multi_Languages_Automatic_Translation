package vn.edu.sgu.audiotour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    // Boot 4 tách auto-cấu hình HTTP client ra module riêng nên context không có sẵn RestClient.Builder
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
