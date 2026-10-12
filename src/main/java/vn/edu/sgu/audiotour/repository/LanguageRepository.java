package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Language;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    
    // Tìm kiếm ngôn ngữ theo mã code (ví dụ: "vi", "en")
    Optional<Language> findByCode(String code);
}