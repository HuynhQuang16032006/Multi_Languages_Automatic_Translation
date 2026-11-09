package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminNarrationService {
    private final NarrationRepository narrations;
    private final LocationRepository locations;
    private final LanguageRepository languages;

    @Transactional
    public Narration create(Long locationId, String code, String text) {
        var location = locations.findById(locationId).orElseThrow(() -> new NotFoundException("Không tìm thấy địa điểm"));
        var language = languages.findByCode(code).orElseThrow(() -> new NotFoundException("Không tìm thấy ngôn ngữ"));
        if (narrations.findByLocationIdAndLanguageCode(locationId, code).isPresent()) {
            throw new ConflictException("Bài thuyết minh đã tồn tại");
        }
        Narration narration = new Narration();
        narration.setLocation(location);
        narration.setLanguage(language);
        narration.setTextContent(text);
        return narrations.save(narration);
    }

    @Transactional
    public Narration update(Long locationId, Long id, String code, String text) {
        Narration narration = find(locationId, id);
        if (!narration.getLanguage().getCode().equals(code)) throw new ConflictException("Không thể đổi ngôn ngữ bài thuyết minh");
        if (!Objects.equals(narration.getTextContent(), text)) {
            narration.setTextContent(text);
            narration.setAudioUrl(null);
            narration.setDurationInSeconds(null);
        }
        return narration;
    }

    @Transactional
    public void delete(Long locationId, Long id) {
        narrations.delete(find(locationId, id));
    }

    private Narration find(Long locationId, Long id) {
        return narrations.findById(id).filter(n -> n.getLocation().getId().equals(locationId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài thuyết minh"));
    }
}
