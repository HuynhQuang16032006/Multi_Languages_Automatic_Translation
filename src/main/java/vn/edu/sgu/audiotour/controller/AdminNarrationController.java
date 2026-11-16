package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.edu.sgu.audiotour.service.AdminNarrationService;
import vn.edu.sgu.audiotour.entity.Narration;

import java.util.List;

@RestController
@RequestMapping("/api/admin/locations/{locationId}/narrations")
@RequiredArgsConstructor
@Tag(name = "7. Quản trị Nội dung Thuyết minh (Admin Narration Controller)", description = "Các API CRUD quản trị nội dung văn bản thuyết minh đa ngôn ngữ theo từng địa điểm POI")
public class AdminNarrationController {
    private final AdminNarrationService service;

    public record Item(Long id, String languageCode, String textContent, String audioUrl, Integer durationInSeconds) {}
    public record Request(@NotBlank @Size(max = 10) String languageCode, @NotBlank @Size(max = 65535) String textContent) {}

    private Item item(Narration n) {
        return new Item(n.getId(), n.getLanguage().getCode(), n.getTextContent(), n.getAudioUrl(), n.getDurationInSeconds());
    }

    @PostMapping
    @Operation(summary = "Tạo mới bài thuyết minh cho địa điểm", description = "Thêm kịch bản thuyết minh mới theo mã ngôn ngữ cho một địa điểm POI")
    public ResponseEntity<Item> create(@PathVariable Long locationId, @Valid @RequestBody Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(item(service.create(locationId, request.languageCode(), request.textContent())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật nội dung bài thuyết minh", description = "Chỉnh sửa kịch bản thuyết minh mà vẫn giữ nguyên liên kết file âm thanh hiện có")
    public Item update(@PathVariable Long locationId, @PathVariable Long id, @Valid @RequestBody Request request) {
        return item(service.update(locationId, id, request.languageCode(), request.textContent()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa bài thuyết minh", description = "Xóa bản ghi thuyết minh theo ID và địa điểm")
    public ResponseEntity<Void> delete(@PathVariable Long locationId, @PathVariable Long id) {
        service.delete(locationId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Liệt kê tất cả bài thuyết minh của 1 địa điểm", description = "Trả về danh sách các bài thuyết minh đa ngôn ngữ thuộc địa điểm POI")
    public List<Item> list(@PathVariable Long locationId) {
        return service.listByLocationId(locationId).stream()
                .map(this::item)
                .toList();
    }
}
