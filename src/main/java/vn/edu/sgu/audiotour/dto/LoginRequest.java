package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request đăng nhập để lấy JWT token")
public class LoginRequest {

    @NotBlank
    @Schema(description = "Tên đăng nhập admin", example = "admin")
    private String username;

    @NotBlank
    @Schema(description = "Mật khẩu admin", example = "admin123")
    private String password;
}
