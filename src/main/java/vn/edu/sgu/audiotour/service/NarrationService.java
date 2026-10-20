package vn.edu.sgu.audiotour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NarrationService {

    private final NarrationRepository narrationRepository;

    // Lấy bài thuyết minh của 1 địa điểm theo mã ngôn ngữ.
    // Trả về DTO gọn thay vì entity để không kéo theo quan hệ lồng (Location.subLocations là lazy)
    public NarrationResponse getNarration(Long locationId, String languageCode) {
        Narration narration = narrationRepository.findByLocationIdAndLanguageCode(locationId, languageCode)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài thuyết minh cho địa điểm " + locationId + " và ngôn ngữ " + languageCode));

        return NarrationResponse.builder()
                .locationId(narration.getLocation().getId())
                .locationName(narration.getLocation().getName())
                .languageCode(narration.getLanguage().getCode())
                .textContent(narration.getTextContent())
                .audioUrl(narration.getAudioUrl())
                .durationInSeconds(narration.getDurationInSeconds())
                .build();
    }
}
