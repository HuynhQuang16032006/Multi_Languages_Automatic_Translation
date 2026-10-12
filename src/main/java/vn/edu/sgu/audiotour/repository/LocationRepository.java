package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    // Lấy danh sách các địa điểm gốc (không có địa điểm cha)
    @EntityGraph(attributePaths = {"subLocations"})
    List<Location> findByParentLocationIsNull();

    // Lấy danh sách các khu vực con của một địa điểm cha
    List<Location> findByParentLocationId(Long parentId);

    // Lấy chi tiết địa điểm kèm các địa điểm con đã được fetch
    @Override
    @EntityGraph(attributePaths = {"subLocations"})
    Optional<Location> findById(Long id);

    // Lấy toàn bộ POI kèm các khu vực con (tránh LazyInitializationException khi open-in-view=false)
    @Override
    @EntityGraph(attributePaths = {"subLocations"})
    List<Location> findAll();

    // Tìm kiếm địa điểm theo mã QR code
    Optional<Location> findByQrCode(String qrCode);

    // Tìm kiếm địa điểm theo tên có chứa từ khóa
    List<Location> findByNameContainingIgnoreCase(String keyword);
}