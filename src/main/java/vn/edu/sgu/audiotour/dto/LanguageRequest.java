package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request tạo mới / cập nhật ngôn ngữ")
public class LanguageRequest {

    @NotBlank
    @Size(max = 10)
    @Schema(description = "Mã ngôn ngữ", example = "vi")
    private String code;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Tên ngôn ngữ", example = "Tiếng Việt")
    private String name;
}
