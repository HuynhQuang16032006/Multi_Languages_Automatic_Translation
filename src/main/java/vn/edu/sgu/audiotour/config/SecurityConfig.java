package vn.edu.sgu.audiotour.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.sgu.audiotour.security.JwtAuthenticationFilter;
import vn.edu.sgu.audiotour.security.JwtService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless dùng JWT: tắt CSRF, tắt httpBasic/formLogin
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/next", "/next/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/v1/auth/login").permitAll()
                        // Dashboard tự gọi thêm ngôn ngữ không cần đăng nhập (trang chỉ admin dùng).
                        // Ngôn ngữ chỉ thêm được 1 lần nên thiệt hại tối đa = số ngôn ngữ trong whitelist.
                        .requestMatchers(HttpMethod.POST, "/api/languages/*/translate").authenticated()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/packages/stream", "/api/packages/offline", "/api/locations/by-qr/**", "/api/analytics/heartbeat", "/api/analytics/logs", "/demo.html", "/index.html", "/player.html", "/dashboard.html", "/favicon.ico").permitAll()
                        .requestMatchers("/api/analytics/dashboard").authenticated()
                        // Trang chủ du khách: "/" serve welcome page (index.html)
                        .requestMatchers("/").permitAll()
                        // Chỉ health là public; metrics/info và các actuator khác phải có token
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // API đọc cho du khách ẩn danh: chỉ mở phương thức GET, API ghi vẫn cần token admin
                        .requestMatchers(HttpMethod.GET, "/api/locations/**", "/api/languages/**", "/api/narrations/**", "/api/v1/locations/**", "/api/v1/languages/**").permitAll()
                        .requestMatchers("/api/**", "/api/v1/**", "/actuator/**").authenticated()
                        .anyRequest().denyAll())
                // Thiếu/sai token trả 401 thay vì 403 mặc định
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                // Tạo filter tại đây (không để @Component) để tránh Boot đăng ký thêm lần nữa và chạy 2 lần/request
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
