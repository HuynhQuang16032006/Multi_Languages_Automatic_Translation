package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.dto.LocationRequest;
import vn.edu.sgu.audiotour.dto.LocationQrRequest;
import vn.edu.sgu.audiotour.dto.LocationVisibilityRequest;
import vn.edu.sgu.audiotour.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "1. Quản lý Địa điểm & POI (Location Controller)", description = "Các API phục vụ lấy danh sách địa danh, cấu trúc cha-con, GPS Geofencing và quét mã QR")
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/locations")
    @Operation(summary = "Tạo địa điểm")
    public ResponseEntity<Location> createLocation(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createLocation(request));
    }

    @PutMapping("/locations/{id}")
    @Operation(summary = "Cập nhật địa điểm")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @PatchMapping("/locations/{id}/qr")
    @Operation(summary = "Cập nhật mã QR địa điểm")
    public ResponseEntity<Location> updateQrCode(@PathVariable Long id, @Valid @RequestBody LocationQrRequest request) {
        return ResponseEntity.ok(locationService.updateQrCode(id, request.qrCode(), request.expectedQrCode()));
    }

    @DeleteMapping("/locations/{id}")
    @Operation(summary = "Xóa địa điểm không có dữ liệu liên quan")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/locations")
    @Operation(summary = "Danh sách địa điểm quản trị, gồm địa điểm ẩn")
    public ResponseEntity<List<Location>> getAdminLocations() {
        return ResponseEntity.ok(locationService.getAdminLocations());
    }

    @PatchMapping("/admin/locations/{id}/visibility")
    @Operation(summary = "Ẩn hoặc hiện địa điểm mà không xóa lịch sử")
    public ResponseEntity<Location> setVisibility(@PathVariable Long id, @Valid @RequestBody LocationVisibilityRequest request) {
        return ResponseEntity.ok(locationService.setVisibility(id, request.visible()));
    }

    @GetMapping("/locations")
    @Operation(summary = "Lấy danh sách các địa điểm gốc", description = "Trả về danh sách các địa danh lớn cấp cao nhất (không có địa điểm cha) kèm các khu vực con")
    public ResponseEntity<List<Location>> getRootLocations() {
        return ResponseEntity.ok(locationService.getRootLocations());
    }

    @GetMapping("/v1/locations")
    @Operation(summary = "Lấy danh sách địa điểm gốc có phân trang (API v1)", description = "Trả về danh sách địa danh dạng Page (page, size, sort) kèm các khu vực con")
    public ResponseEntity<Page<Location>> getLocations(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(locationService.getLocations(pageable));
    }

    @GetMapping("/locations/pois")
    @Operation(summary = "Đồng bộ toàn bộ POI cho Mobile App (Geofencing)", description = "Trả về danh sách tất cả các điểm quan tâm (POI) có tọa độ GPS, bán kính geofence, độ ưu tiên để App lưu trữ SQLite offline")
    public ResponseEntity<List<Location>> getAllPois() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/locations/{id}")
    @Operation(summary = "Lấy chi tiết 1 địa điểm theo ID", description = "Truy xuất thông tin chi tiết của địa danh hoặc phòng ban cụ thể")
    public ResponseEntity<Location> getLocationById(
            @Parameter(description = "ID của địa điểm", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @GetMapping("/locations/by-qr/{qrCode}")
    @Operation(summary = "Quét mã QR để lấy thông tin địa điểm", description = "Mobile App quét mã QR dán trên tường/trạm dừng xe buýt và gọi API này để nhận thông tin POI")
    public ResponseEntity<Location> getLocationByQrCode(
            @Parameter(description = "Mã QR code quét được", example = "QR-KHANH-HOI-01") @PathVariable String qrCode) {
        return ResponseEntity.ok(locationService.getLocationByQrCode(qrCode));
    }

    @GetMapping("/locations/{id}/sub-locations")
    @Operation(summary = "Lấy danh sách các khu vực con của 1 địa điểm", description = "Ví dụ: Lấy các phòng ban (Phòng Khánh Tiết, Phòng Đại Yến) bên trong Dinh Độc Lập")
    public ResponseEntity<List<Location>> getSubLocations(
            @Parameter(description = "ID của địa điểm cha", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(locationService.getSubLocations(id));
    }
}
