package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import vn.edu.sgu.audiotour.dto.LanguageRequest;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.LanguageService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private NarrationRepository narrationRepository;

    @InjectMocks
    private LanguageService languageService;

    private LanguageRequest request(String code, String name) {
        LanguageRequest request = new LanguageRequest();
        request.setCode(code);
        request.setName(name);
        return request;
    }

    @Test
    void testGetAllLanguages() {
        when(languageRepository.findAll()).thenReturn(List.of(new Language(1L, "vi", "Tiếng Việt")));

        List<Language> result = languageService.getAllLanguages();

        assertEquals(1, result.size());
        assertEquals("vi", result.get(0).getCode());
    }

    @Test
    void testGetLanguagesPaged() {
        Page<Language> page = new PageImpl<>(List.of(new Language(1L, "vi", "Tiếng Việt")),
                PageRequest.of(0, 20), 1);
        when(languageRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

        Page<Language> result = languageService.getLanguages(PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals("vi", result.getContent().get(0).getCode());
    }

    @Test
    void testGetLanguageById_Found() {
        when(languageRepository.findById(1L)).thenReturn(Optional.of(new Language(1L, "vi", "Tiếng Việt")));

        Language result = languageService.getLanguageById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetLanguageById_NotFound() {
        when(languageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> languageService.getLanguageById(999L));
    }

    @Test
    void testCreateLanguage_Success() {
        when(languageRepository.findByCode("fr")).thenReturn(Optional.empty());
        when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Language result = languageService.createLanguage(request("fr", "Français"));

        assertEquals("fr", result.getCode());
        assertEquals("Français", result.getName());
    }

    @Test
    void testCreateLanguage_DuplicateCode() {
        when(languageRepository.findByCode("vi")).thenReturn(Optional.of(new Language(1L, "vi", "Tiếng Việt")));

        assertThrows(ConflictException.class, () -> languageService.createLanguage(request("vi", "Tiếng Việt")));
        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void testUpdateLanguage_Success() {
        Language existing = new Language(1L, "vi", "Tiếng Việt");
        when(languageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(languageRepository.findByCode("en")).thenReturn(Optional.empty());
        when(languageRepository.save(existing)).thenReturn(existing);

        Language result = languageService.updateLanguage(1L, request("en", "English"));

        assertEquals("en", result.getCode());
        assertEquals("English", result.getName());
    }

    @Test
    void testUpdateLanguage_NotFound() {
        when(languageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> languageService.updateLanguage(999L, request("vi", "Tiếng Việt")));
    }

    @Test
    void testDeleteLanguage_Success() {
        when(languageRepository.existsById(1L)).thenReturn(true);

        languageService.deleteLanguage(1L);

        verify(languageRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteLanguage_NotFound() {
        when(languageRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> languageService.deleteLanguage(999L));
        verify(languageRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteLanguage_ReferencedByNarration_ThrowsConflict() {
        when(languageRepository.existsById(1L)).thenReturn(true);
        when(narrationRepository.existsByLanguageId(1L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> languageService.deleteLanguage(1L));

        assertTrue(ex.getMessage().contains("thuyết minh"));
        verify(languageRepository, never()).deleteById(anyLong());
    }
}
