package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.audiotour.entity.Tour;
import vn.edu.sgu.audiotour.service.TourService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "4. Quản lý Tuyến tham quan (Tour Controller)", description = "Các API phục vụ lấy danh sách tuyến Tour du lịch (Tour cơ bản, Tour dạo mát, Tour di tích)")
public class TourController {

    private final TourService tourService;

    @GetMapping("/tours")
    @Operation(summary = "Lấy toàn bộ danh sách các Tour", description = "Trả về danh sách các tour du lịch có sẵn kèm danh sách địa điểm POI bên trong")
    public ResponseEntity<List<Tour>> getAllTours() {
        return ResponseEntity.ok(tourService.getAllTours());
    }

    @GetMapping("/v1/tours")
    @Operation(summary = "Lấy danh sách Tour có phân trang (API v1)", description = "Trả về danh sách tour dạng Page (page, size, sort) kèm lộ trình các điểm dừng")
    public ResponseEntity<Page<Tour>> getTours(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(tourService.getTours(pageable));
    }

    @GetMapping("/tours/{id}")
    @Operation(summary = "Lấy chi tiết 1 Tour theo ID", description = "Truy xuất thông tin chi tiết một tuyến tour kèm lộ trình các điểm dừng")
    public ResponseEntity<Tour> getTourById(
            @Parameter(description = "ID của Tour", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }
}
