package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.config.LanguageProperties;
import vn.edu.sgu.audiotour.config.LanguageProperties.SupportedLanguage;
import vn.edu.sgu.audiotour.dto.AddLanguageResponse;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.ServiceUnavailableException;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.LanguageOnboardingService;
import vn.edu.sgu.audiotour.service.LanguageService;
import vn.edu.sgu.audiotour.service.TranslationService;
import vn.edu.sgu.audiotour.service.TtsService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageOnboardingServiceTest {

    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private NarrationRepository narrationRepository;
    @Mock
    private LanguageService languageService;
    @Mock
    private TranslationService translationService;
    @Mock
    private TtsService ttsService;

    private LanguageOnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        LanguageProperties properties = new LanguageProperties(
                List.of(new SupportedLanguage("ja", "日本語"), new SupportedLanguage("ko", "한국어")));
        onboardingService = new LanguageOnboardingService(properties, languageRepository,
                narrationRepository, languageService, translationService, ttsService);
    }

    @Test
    void testAdd_UnsupportedCodeThrowsIllegalArgument() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> onboardingService.add("xx"));

        assertTrue(exception.getMessage().contains("xx"));
        verify(translationService, never()).translate(anyString(), anyString());
    }

    @Test
    void testAdd_ExistingLanguageThrowsConflictBeforeTranslating() {
        when(languageRepository.findByCode("ja")).thenReturn(Optional.of(new Language(9L, "ja", "日本語")));

        assertThrows(ConflictException.class, () -> onboardingService.add("ja"));
        verify(translationService, never()).translate(anyString(), anyString());
    }

    @Test
    void testAdd_TranslationFailureSavesNothing() {
        when(languageRepository.findByCode("ja")).thenReturn(Optional.empty());
        when(narrationRepository.findAllByLanguageCode("vi")).thenReturn(List.of(source(1L), source(2L)));
        when(translationService.translate(anyString(), eq("ja")))
                .thenReturn("bản dịch 1")
                .thenThrow(new ServiceUnavailableException("DeepL lỗi 500"));

        assertThrows(ServiceUnavailableException.class, () -> onboardingService.add("ja"));

        verify(languageService, never()).saveWithNarrations(any(), anyList());
        verify(ttsService, never()).generateAudio(anyLong(), eq(false));
    }

    @Test
    void testAdd_SuccessReturnsTranslationAndAudioCounts() {
        when(languageRepository.findByCode("ja")).thenReturn(Optional.empty());
        when(narrationRepository.findAllByLanguageCode("vi")).thenReturn(List.of(source(1L), source(2L)));
        when(translationService.translate(anyString(), eq("ja"))).thenReturn("bản dịch");
        when(languageService.saveWithNarrations(any(), anyList())).thenReturn(new Language(4L, "ja", "日本語"));

        AddLanguageResponse response = onboardingService.add("ja");

        assertEquals(2, response.translations());
        assertEquals(2, response.audioGenerated());
        assertEquals("ja", response.language().getCode());
    }

    @Test
    void testAdd_AudioFailureIsCountedNotThrown() {
        when(languageRepository.findByCode("ja")).thenReturn(Optional.empty());
        when(narrationRepository.findAllByLanguageCode("vi")).thenReturn(List.of(source(1L), source(2L)));
        when(translationService.translate(anyString(), eq("ja"))).thenReturn("bản dịch");
        when(languageService.saveWithNarrations(any(), anyList())).thenAnswer(invocation -> {
            List<Narration> saved = invocation.getArgument(1);
            for (int i = 0; i < saved.size(); i++) {
                saved.get(i).setId(10L + i);
            }
            return new Language(4L, "ja", "日本語");
        });
        when(ttsService.generateAudio(anyLong(), eq(false)))
                .thenReturn(null)
                .thenThrow(new ServiceUnavailableException("hết quota"));

        AddLanguageResponse response = onboardingService.add("ja");

        assertEquals(2, response.translations());
        assertEquals(1, response.audioGenerated());
    }

    @Test
    void testAdd_NoSourceNarrationThrowsIllegalArgument() {
        when(languageRepository.findByCode("ja")).thenReturn(Optional.empty());
        when(narrationRepository.findAllByLanguageCode("vi")).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> onboardingService.add("ja"));
        verify(languageService, never()).saveWithNarrations(any(), anyList());
    }

    private Narration source(Long id) {
        Location location = new Location();
        location.setId(id + 100);
        Narration narration = new Narration();
        narration.setId(id);
        narration.setLocation(location);
        narration.setLanguage(new Language(1L, "vi", "Tiếng Việt"));
        narration.setTextContent("Nội dung tiếng Việt " + id);
        return narration;
    }
}
