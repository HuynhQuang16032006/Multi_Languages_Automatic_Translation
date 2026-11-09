package vn.edu.sgu.audiotour.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/admin/locations/{locationId}/narrations")
@RequiredArgsConstructor
public class AdminNarrationController {
    private final NarrationRepository narrations;
    private final LocationRepository locations;

    public record Item(Long id, String languageCode, String textContent, String audioUrl, Integer durationInSeconds) {}

    @GetMapping
    @Transactional(readOnly = true)
    public List<Item> list(@PathVariable Long locationId) {
        if (!locations.existsById(locationId)) {
            throw new NotFoundException("Không tìm thấy địa điểm với ID: " + locationId);
        }
        return narrations.findAllByLocationId(locationId).stream()
                .map(n -> new Item(n.getId(), n.getLanguage().getCode(), n.getTextContent(), n.getAudioUrl(), n.getDurationInSeconds()))
                .toList();
    }
}
