package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.NarrationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NarrationServiceTest {

    @Mock
    private NarrationRepository narrationRepository;

    @InjectMocks
    private NarrationService narrationService;

    @Test
    void testGetNarration_Found() {
        Location location = new Location();
        location.setId(5L);
        location.setName("Dinh Độc Lập");

        Language language = new Language();
        language.setId(1L);
        language.setCode("vi");
        language.setName("Tiếng Việt");

        Narration narration = new Narration();
        narration.setId(10L);
        narration.setLocation(location);
        narration.setLanguage(language);
        narration.setTextContent("Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập.");
        narration.setAudioUrl("dinh-doc-lap-vi.mp3");
        narration.setDurationInSeconds(95);

        when(narrationRepository.findByLocationIdAndLanguageCode(5L, "vi"))
                .thenReturn(Optional.of(narration));

        NarrationResponse result = narrationService.getNarration(5L, "vi");

        assertNotNull(result);
        assertEquals(5L, result.getLocationId());
        assertEquals("Dinh Độc Lập", result.getLocationName());
        assertEquals("vi", result.getLanguageCode());
        assertEquals("Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập.", result.getTextContent());
        assertEquals("dinh-doc-lap-vi.mp3", result.getAudioUrl());
        assertEquals(95, result.getDurationInSeconds());
    }

    @Test
    void testGetNarration_NotFound() {
        when(narrationRepository.findByLocationIdAndLanguageCode(999L, "xx"))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                narrationService.getNarration(999L, "xx")
        );

        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("xx"));
    }
}
