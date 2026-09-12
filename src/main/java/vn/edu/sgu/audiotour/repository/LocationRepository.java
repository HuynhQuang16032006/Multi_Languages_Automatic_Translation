package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Location;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Tìm kiếm địa điểm theo tên có chứa từ khóa (phục vụ thanh tìm kiếm trên App)
    List<Location> findByNameContainingIgnoreCase(String keyword);
}