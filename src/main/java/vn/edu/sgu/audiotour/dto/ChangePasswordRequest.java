package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request đổi mật khẩu admin")
public class ChangePasswordRequest {

    @NotBlank
    @Schema(description = "Mật khẩu cũ", example = "admin123")
    private String oldPassword;

    @NotBlank
    @Size(min = 6, message = "Mật khẩu mới phải có tối thiểu 6 ký tự")
    @Schema(description = "Mật khẩu mới", example = "newAdminPass456")
    private String newPassword;
}
