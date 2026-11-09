package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Narration;

import java.util.List;
import java.util.Optional;

@Repository
public interface NarrationRepository extends JpaRepository<Narration, Long> {

    boolean existsByLocationId(Long locationId);

    @EntityGraph(attributePaths = {"language"})
    List<Narration> findAllByLocationId(Long locationId);

    // Lấy bài thuyết minh của 1 địa điểm cụ thể bằng 1 mã ngôn ngữ cụ thể
    @EntityGraph(attributePaths = {"location", "language"})
    Optional<Narration> findByLocationIdAndLanguageCode(Long locationId, String languageCode);

    // Nguồn dịch khi thêm ngôn ngữ mới: toàn bộ bản thuyết minh của 1 ngôn ngữ
    @EntityGraph(attributePaths = {"location", "language"})
    List<Narration> findAllByLanguageCode(String languageCode);

    // Kiểm tra ngôn ngữ có đang được dùng trong bản thuyết minh nào không (chặn xóa gây lỗi khóa ngoại)
    boolean existsByLanguageId(Long languageId);
}
