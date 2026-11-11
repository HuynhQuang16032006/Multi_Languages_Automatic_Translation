package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import org.springframework.beans.BeanUtils;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;
    private final NarrationRepository narrationRepository;
    private final PlaybackLogRepository playbackLogRepository;
    private final EntityManager entityManager;

    private void lockHierarchy() {
        entityManager.createNativeQuery("select id from location_hierarchy_lock where id = 1 for update").getSingleResult();
    }

    private Location publicLocation(Location location) {
        Location copy = new Location();
        BeanUtils.copyProperties(location, copy);
        copy.setSubLocations(location.getSubLocations().stream().filter(Location::isVisible).map(child -> {
            Location childCopy = new Location();
            BeanUtils.copyProperties(child, childCopy);
            childCopy.setSubLocations(List.of());
            return childCopy;
        }).toList());
        return copy;
    }

    @Transactional
    public Location createLocation(LocationRequest request) {
        lockHierarchy();
        return saveLocation(new Location(), request);
    }

    @Transactional
    public Location updateLocation(Long id, LocationRequest request) {
        lockHierarchy();
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + id));
        return saveLocation(location, request);
    }

    @Transactional
    public Location updateQrCode(Long id, String qrCode, String expectedQrCode) {
        lockHierarchy();
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + id));
        if (!Objects.equals(location.getQrCode(), expectedQrCode)) {
            throw new ConflictException("Mã QR đã thay đổi");
        }
        if (locationRepository.findByQrCode(qrCode)
                .filter(other -> !other.getId().equals(id)).isPresent()) {
            throw new ConflictException("Mã QR đã tồn tại: " + qrCode);
        }
        entityManager.createQuery("update Location l set l.qrCode = :qrCode where l.id = :id")
                .setParameter("qrCode", qrCode).setParameter("id", id).executeUpdate();
        entityManager.refresh(location);
        return location;
    }

    private Location saveLocation(Location location, LocationRequest request) {
        if (request.getQrCode() != null && !Objects.equals(request.getQrCode(), location.getQrCode())
                && !request.getQrCode().matches("[A-Za-z0-9_-]{1,50}")) {
            throw new IllegalArgumentException("Mã QR chỉ được chứa chữ, số, dấu gạch ngang hoặc gạch dưới");
        }
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
            if (location.isVisible() && !parent.isVisible()) {
                throw new ConflictException("Không thể đặt địa điểm hiện dưới địa điểm cha ẩn");
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
        lockHierarchy();
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
        return locationRepository.findByParentLocationIsNull().stream().filter(Location::isVisible).map(this::publicLocation).toList();
    }

    // Lấy danh sách địa điểm có phân trang, sắp xếp (API v1)
    public Page<Location> getLocations(Pageable pageable) {
        return locationRepository.findAllPaged(pageable).map(this::publicLocation);
    }

    // Lấy chi tiết 1 địa điểm theo ID
    public Location getLocationById(Long id) {
        return locationRepository.findById(id).filter(Location::isVisible).map(this::publicLocation)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + id));
    }

    // Tìm kiếm địa điểm theo mã QR (quét QR code tại điểm dừng / di tích)
    public Location getLocationByQrCode(String qrCode) {
        return locationRepository.findByQrCode(qrCode).filter(Location::isVisible).map(this::publicLocation)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm gắn với mã QR: " + qrCode));
    }

    // Lấy danh sách các khu vực con của 1 địa điểm
    public List<Location> getSubLocations(Long parentId) {
        getLocationById(parentId);
        return locationRepository.findByParentLocationId(parentId).stream().filter(Location::isVisible).map(this::publicLocation).toList();
    }

    // Lấy toàn bộ địa điểm (POI) bao gồm cả tọa độ, bán kính geofence để app đồng bộ offline
    public List<Location> getAllLocations() {
        return locationRepository.findAll().stream().filter(Location::isVisible).map(this::publicLocation).toList();
    }

    public List<Location> getAdminLocations() {
        return locationRepository.findAll();
    }

    @Transactional
    public Location setVisibility(Long id, boolean visible) {
        lockHierarchy();
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm với ID: " + id));
        if (visible && location.getParentLocation() != null && !location.getParentLocation().isVisible()) {
            throw new ConflictException("Không thể hiện địa điểm khi địa điểm cha đang ẩn");
        }
        if (!visible && locationRepository.existsByParentLocationIdAndVisibleTrue(id)) {
            throw new ConflictException("Không thể ẩn địa điểm có địa điểm con đang hiện");
        }
        location.setVisible(visible);
        return locationRepository.save(location);
    }
}
