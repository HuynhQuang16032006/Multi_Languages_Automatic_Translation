package vn.edu.sgu.audiotour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Nguồn thời gian của ứng dụng. Tách thành bean để test biểu thức hết hạn
 * (heartbeat) bằng một Clock cố định thay vì phải sleep.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
