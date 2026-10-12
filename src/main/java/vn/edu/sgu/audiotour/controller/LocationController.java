package vn.edu.sgu.audiotour.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "1. Quản lý Địa điểm & POI (Location Controller)", description = "Các API phục vụ lấy danh sách địa danh, cấu trúc cha-con, GPS Geofencing và quét mã QR")
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách các địa điểm gốc", description = "Trả về danh sách các địa danh lớn cấp cao nhất (không có địa điểm cha) kèm các khu vực con")
    public ResponseEntity<List<Location>> getRootLocations() {
        return ResponseEntity.ok(locationService.getRootLocations());
    }

    @GetMapping("/pois")
    @Operation(summary = "Đồng bộ toàn bộ POI cho Mobile App (Geofencing)", description = "Trả về danh sách tất cả các điểm quan tâm (POI) có tọa độ GPS, bán kính geofence, độ ưu tiên để App lưu trữ SQLite offline")
    public ResponseEntity<List<Location>> getAllPois() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết 1 địa điểm theo ID", description = "Truy xuất thông tin chi tiết của địa danh hoặc phòng ban cụ thể")
    public ResponseEntity<Location> getLocationById(
            @Parameter(description = "ID của địa điểm", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @GetMapping("/by-qr/{qrCode}")
    @Operation(summary = "Quét mã QR để lấy thông tin địa điểm", description = "Mobile App quét mã QR dán trên tường/trạm dừng xe buýt và gọi API này để nhận thông tin POI")
    public ResponseEntity<Location> getLocationByQrCode(
            @Parameter(description = "Mã QR code quét được", example = "QR-KHANH-HOI-01") @PathVariable String qrCode) {
        return ResponseEntity.ok(locationService.getLocationByQrCode(qrCode));
    }

    @GetMapping("/{id}/sub-locations")
    @Operation(summary = "Lấy danh sách các khu vực con của 1 địa điểm", description = "Ví dụ: Lấy các phòng ban (Phòng Khánh Tiết, Phòng Đại Yến) bên trong Dinh Độc Lập")
    public ResponseEntity<List<Location>> getSubLocations(
            @Parameter(description = "ID của địa điểm cha", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(locationService.getSubLocations(id));
    }
}