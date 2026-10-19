package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response trả về JWT token sau khi đăng nhập thành công")
public class LoginResponse {

    @Schema(description = "JWT token dùng cho header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Loại token", example = "Bearer")
    private String tokenType;
}
