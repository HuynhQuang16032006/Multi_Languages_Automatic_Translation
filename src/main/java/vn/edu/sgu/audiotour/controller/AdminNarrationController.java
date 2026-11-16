package vn.edu.sgu.audiotour.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.AdminNarrationService;
import vn.edu.sgu.audiotour.entity.Narration;

import java.util.List;

@RestController
@RequestMapping("/api/admin/locations/{locationId}/narrations")
@RequiredArgsConstructor
public class AdminNarrationController {
    private final NarrationRepository narrations;
    private final LocationRepository locations;
    private final AdminNarrationService service;

    public record Item(Long id, String languageCode, String textContent, String audioUrl, Integer durationInSeconds) {}
    public record Request(@NotBlank @Size(max = 10) String languageCode, @NotBlank @Size(max = 65535) String textContent) {}

    private Item item(Narration n) {
        return new Item(n.getId(), n.getLanguage().getCode(), n.getTextContent(), n.getAudioUrl(), n.getDurationInSeconds());
    }

    @PostMapping
    public ResponseEntity<Item> create(@PathVariable Long locationId, @Valid @RequestBody Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(item(service.create(locationId, request.languageCode(), request.textContent())));
    }

    @PutMapping("/{id}")
    public Item update(@PathVariable Long locationId, @PathVariable Long id, @Valid @RequestBody Request request) {
        return item(service.update(locationId, id, request.languageCode(), request.textContent()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long locationId, @PathVariable Long id) {
        service.delete(locationId, id);
        return ResponseEntity.noContent().build();
    }

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
