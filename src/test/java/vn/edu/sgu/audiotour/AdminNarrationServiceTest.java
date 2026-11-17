package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.AdminNarrationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử đơn vị (Unit Test) tầng Service cho AdminNarrationService.
 */
@ExtendWith(MockitoExtension.class)
class AdminNarrationServiceTest {

    @Mock
    private NarrationRepository narrationRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private AdminNarrationService adminNarrationService;

    @Test
    @DisplayName("listByLocationId: trả về danh sách bài thuyết minh khi địa điểm tồn tại")
    void listByLocationIdShouldReturnNarrationsWhenLocationExists() {
        Location loc = new Location();
        loc.setId(1L);
        Language lang = new Language(1L, "vi", "Tiếng Việt");
        Narration n = new Narration(10L, "Nội dung thuyết minh", "1-vi.mp3", 45, loc, lang);

        when(locationRepository.existsById(1L)).thenReturn(true);
        when(narrationRepository.findAllByLocationId(1L)).thenReturn(List.of(n));

        List<Narration> result = adminNarrationService.listByLocationId(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTextContent()).isEqualTo("Nội dung thuyết minh");
    }

    @Test
    @DisplayName("listByLocationId: ném NotFoundException khi địa điểm không tồn tại")
    void listByLocationIdShouldThrowNotFoundWhenLocationMissing() {
        when(locationRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> adminNarrationService.listByLocationId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("create: lưu bài thuyết minh mới khi cặp địa điểm và ngôn ngữ chưa tồn tại")
    void createShouldSaveNewNarrationWhenUnique() {
        Location loc = new Location();
        loc.setId(4L);
        Language lang = new Language(2L, "en", "English");

        when(locationRepository.findById(4L)).thenReturn(Optional.of(loc));
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(lang));
        when(narrationRepository.findByLocationIdAndLanguageCode(4L, "en")).thenReturn(Optional.empty());
        when(narrationRepository.save(any(Narration.class))).thenAnswer(inv -> inv.getArgument(0));

        Narration saved = adminNarrationService.create(4L, "en", "Independence Palace narration");
        assertThat(saved.getLocation()).isEqualTo(loc);
        assertThat(saved.getLanguage()).isEqualTo(lang);
        assertThat(saved.getTextContent()).isEqualTo("Independence Palace narration");
    }

    @Test
    @DisplayName("create: ném ConflictException khi bài thuyết minh cho ngôn ngữ đó đã tồn tại")
    void createShouldThrowConflictWhenDuplicate() {
        Location loc = new Location();
        loc.setId(4L);
        Language lang = new Language(2L, "en", "English");

        when(locationRepository.findById(4L)).thenReturn(Optional.of(loc));
        when(languageRepository.findByCode("en")).thenReturn(Optional.of(lang));
        when(narrationRepository.findByLocationIdAndLanguageCode(4L, "en"))
                .thenReturn(Optional.of(new Narration()));

        assertThatThrownBy(() -> adminNarrationService.create(4L, "en", "Duplicate text"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("update: giữ nguyên audioUrl khi chỉ cập nhật nội dung văn bản")
    void updateShouldKeepExistingAudioUrlWhenUpdatingText() {
        Location loc = new Location();
        loc.setId(4L);
        Language lang = new Language(1L, "vi", "Tiếng Việt");
        Narration existing = new Narration(15L, "Văn bản cũ", "4-vi.mp3", 60, loc, lang);

        when(narrationRepository.findById(15L)).thenReturn(Optional.of(existing));

        Narration updated = adminNarrationService.update(4L, 15L, "vi", "Văn bản mới đã chỉnh sửa");
        assertThat(updated.getTextContent()).isEqualTo("Văn bản mới đã chỉnh sửa");
        assertThat(updated.getAudioUrl()).isEqualTo("4-vi.mp3");
    }

    @Test
    @DisplayName("delete: xóa bài thuyết minh khi khớp locationId và id")
    void deleteShouldRemoveNarrationWhenMatched() {
        Location loc = new Location();
        loc.setId(4L);
        Language lang = new Language(1L, "vi", "Tiếng Việt");
        Narration existing = new Narration(15L, "Văn bản", "4-vi.mp3", 60, loc, lang);

        when(narrationRepository.findById(15L)).thenReturn(Optional.of(existing));

        adminNarrationService.delete(4L, 15L);
        verify(narrationRepository).delete(existing);
    }
}
