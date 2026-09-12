package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Narration;
import java.util.Optional;

@Repository
public interface NarrationRepository extends JpaRepository<Narration, Long> {
    // Lấy bài thuyết minh của 1 địa điểm cụ thể bằng 1 ngôn ngữ cụ thể
    Optional<Narration> findByLocationIdAndLanguageCode(Long locationId, String languageCode);
}