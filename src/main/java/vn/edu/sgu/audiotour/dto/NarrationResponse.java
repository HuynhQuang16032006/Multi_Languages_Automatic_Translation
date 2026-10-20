package vn.edu.sgu.audiotour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bài thuyết minh (văn bản + file âm thanh) của 1 địa điểm theo ngôn ngữ")
public class NarrationResponse {

    @Schema(description = "Mã địa danh của địa điểm (POI)", example = "5")
    private Long locationId;

    @Schema(description = "Tên địa điểm / POI", example = "Dinh Độc Lập")
    private String locationName;

    @Schema(description = "Mã ngôn ngữ của bản thuyết minh", example = "vi")
    private String languageCode;

    @Schema(description = "Toàn văn bản thuyết minh của địa điểm", example = "Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập của Việt Nam.")
    private String textContent;

    @Schema(description = "Tên/đường dẫn file âm thanh MP3 của bản thuyết minh", example = "dinh-doc-lap-vi.mp3")
    private String audioUrl;

    @Schema(description = "Thời lượng file âm thanh (giây)", example = "95")
    private Integer durationInSeconds;
}
