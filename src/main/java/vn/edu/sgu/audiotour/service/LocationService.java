package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.dto.LocationRequest;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;
    private final NarrationRepository narrationRepository;
    private final PlaybackLogRepository playbackLogRepository;

    @Transactional
    public Location createLocation(LocationRequest request) {
        return saveLocation(new Location(), request);
    }

    @Transactional
    public Location updateLocation(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + id));
        return saveLocation(location, request);
    }

    private Location saveLocation(Location location, LocationRequest request) {
        if (request.getQrCode() != null && locationRepository.findByQrCode(request.getQrCode())
                .filter(other -> !other.getId().equals(location.getId())).isPresent()) {
            throw new ConflictException("Mã QR đã tồn tại: " + request.getQrCode());
        }
        Location parent = null;
        if (request.getParentId() != null) {
            parent = locationRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm cha với ID: " + request.getParentId()));
            for (Location ancestor = parent; ancestor != null; ancestor = ancestor.getParentLocation()) {
                if (ancestor.getId().equals(location.getId())) {
                    throw new ConflictException("Quan hệ địa điểm cha tạo thành chu trình");
                }
            }
        }
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        if (request.getRadiusInMeters() != null) location.setRadiusInMeters(request.getRadiusInMeters());
        if (request.getPriority() != null) location.setPriority(request.getPriority());
        location.setQrCode(request.getQrCode());
        location.setImageUrl(request.getImageUrl());
        location.setParentLocation(parent);
        return locationRepository.save(location);
    }

    @Transactional
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy địa điểm với ID: " + id);
        }
        if (locationRepository.existsByParentLocationId(id)
                || narrationRepository.existsByLocationId(id)
                || playbackLogRepository.existsByLocationId(id)) {
            throw new ConflictException("Không thể xóa địa điểm có địa điểm con, thuyết minh hoặc lịch sử nghe: " + id);
        }
        locationRepository.deleteById(id);
    }

    // Lấy tất cả các địa điểm gốc (root locations - không có parent)
    public List<Location> getRootLocations() {
        return locationRepository.findByParentLocationIsNull();
    }

    // Lấy danh sách địa điểm có phân trang, sắp xếp (API v1)
    public Page<Location> getLocations(Pageable pageable) {
        return locationRepository.findAllPaged(pageable);
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
