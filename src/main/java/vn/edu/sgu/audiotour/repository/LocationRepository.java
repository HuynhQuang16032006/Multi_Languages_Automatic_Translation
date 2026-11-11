package vn.edu.sgu.audiotour.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByParentLocationId(Long parentId);

    boolean existsByParentLocationIdAndVisibleTrue(Long parentId);

    // Lấy danh sách các địa điểm gốc (không có địa điểm cha)
    @EntityGraph(attributePaths = {"subLocations"})
    List<Location> findByParentLocationIsNull();

    // Lấy danh sách các khu vực con của một địa điểm cha
    // (kèm khu vực con đã fetch để tránh LazyInitializationException khi open-in-view=false)
    @EntityGraph(attributePaths = {"subLocations"})
    List<Location> findByParentLocationId(Long parentId);

    // Lấy chi tiết địa điểm kèm các địa điểm con đã được fetch
    @Override
    @EntityGraph(attributePaths = {"subLocations"})
    Optional<Location> findById(Long id);

    // Lấy toàn bộ POI kèm các khu vực con (tránh LazyInitializationException khi open-in-view=false)
    @Override
    @EntityGraph(attributePaths = {"subLocations"})
    List<Location> findAll();

    // Lấy địa điểm có phân trang kèm khu vực con.
    // shortcut: fetch join + phân trang nên Hibernate phân trang trong bộ nhớ và tải hết dữ liệu (HHH90003004); nâng cấp bằng cách phân trang theo id rồi fetch theo id.
    @Query(value = "select distinct l from Location l where l.visible = true",
            countQuery = "select count(l) from Location l where l.visible = true")
    @EntityGraph(attributePaths = {"subLocations"})
    Page<Location> findAllPaged(Pageable pageable);

    // Tìm kiếm địa điểm theo mã QR code
    @EntityGraph(attributePaths = {"subLocations"})
    Optional<Location> findByQrCode(String qrCode);

    // Tìm kiếm địa điểm theo tên có chứa từ khóa
    List<Location> findByNameContainingIgnoreCase(String keyword);
}
