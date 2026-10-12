package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    // Lấy tất cả các địa điểm gốc (root locations - không có parent)
    public List<Location> getRootLocations() {
        return locationRepository.findByParentLocationIsNull();
    }

    // Lấy chi tiết 1 địa điểm theo ID
    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm với ID: " + id));
    }

    // Tìm kiếm địa điểm theo mã QR (quét QR code tại điểm dừng / di tích)
    public Location getLocationByQrCode(String qrCode) {
        return locationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm gắn với mã QR: " + qrCode));
    }

    // Lấy danh sách các khu vực con của 1 địa điểm
    public List<Location> getSubLocations(Long parentId) {
        if (!locationRepository.existsById(parentId)) {
            throw new RuntimeException("Không tìm thấy địa điểm cha với ID: " + parentId);
        }
        return locationRepository.findByParentLocationId(parentId);
    }

    // Lấy toàn bộ địa điểm (POI) bao gồm cả tọa độ, bán kính geofence để app đồng bộ offline
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}