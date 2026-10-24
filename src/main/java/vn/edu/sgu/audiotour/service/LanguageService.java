package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.LanguageRequest;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LanguageService {

    private final LanguageRepository languageRepository;
    private final NarrationRepository narrationRepository;

    // Lấy toàn bộ ngôn ngữ (dạng danh sách phẳng)
    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }

    // Lấy ngôn ngữ có phân trang, sắp xếp
    public Page<Language> getLanguages(Pageable pageable) {
        return languageRepository.findAll(pageable);
    }

    // Lấy chi tiết 1 ngôn ngữ theo ID
    public Language getLanguageById(Long id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ngôn ngữ với ID: " + id));
    }

    // Tạo mới ngôn ngữ. Pre-check cho trường hợp thường gặp; trường hợp race sẽ do
    // DataIntegrityViolationException của unique constraint được GlobalExceptionHandler dịch thành 409.
    @Transactional
    public Language createLanguage(LanguageRequest request) {
        if (languageRepository.findByCode(request.getCode()).isPresent()) {
            throw new ConflictException("Mã ngôn ngữ đã tồn tại: " + request.getCode());
        }
        return languageRepository.save(new Language(null, request.getCode(), request.getName()));
    }

    // Ghi Language và toàn bộ Narration đã dịch trong 1 transaction: lỗi giữa đường thì DB không đổi
    @Transactional
    public Language saveWithNarrations(Language language, List<Narration> narrations) {
        Language saved = languageRepository.save(language);
        narrations.forEach(narration -> {
            narration.setLanguage(saved);
            narration.setAudioUrl(null);
        });
        narrationRepository.saveAll(narrations);
        return saved;
    }

    // Cập nhật ngôn ngữ theo ID
    @Transactional
    public Language updateLanguage(Long id, LanguageRequest request) {
        Language language = getLanguageById(id);
        languageRepository.findByCode(request.getCode())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Mã ngôn ngữ đã tồn tại: " + request.getCode());
                });
        language.setCode(request.getCode());
        language.setName(request.getName());
        return languageRepository.save(language);
    }

    // Xóa ngôn ngữ theo ID, chặn xóa nếu còn bản thuyết minh đang tham chiếu
    @Transactional
    public void deleteLanguage(Long id) {
        if (!languageRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy ngôn ngữ với ID: " + id);
        }
        if (narrationRepository.existsByLanguageId(id)) {
            throw new ConflictException("Không thể xóa ngôn ngữ vì đang được dùng trong bản thuyết minh: " + id);
        }
        languageRepository.deleteById(id);
    }
}
