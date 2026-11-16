package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.*;
import vn.edu.sgu.audiotour.repository.*;
import vn.edu.sgu.audiotour.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocationDeleteIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private LocationRepository locations;
    @Autowired private NarrationRepository narrations;
    @Autowired private LanguageRepository languages;
    @Autowired private PlaybackLogRepository logs;
    @Autowired private JwtService jwt;
    @Autowired private AdminAuthRepository admins;

    private Location location(String name) {
        Location location = new Location();
        location.setName(name);
        return locations.saveAndFlush(location);
    }

    private String token() {
        return "Bearer " + jwt.generateToken("admin", admins.findById(1).orElseThrow().getPasswordHash());
    }

    @Test
    void requiresValidToken() throws Exception {
        Location location = location("Protected");
        mvc.perform(delete("/api/locations/{id}", location.getId())).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/locations/{id}", location.getId()).header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        assertTrue(locations.existsById(location.getId()));
    }

    @Test
    void rejectsDeletionWithChildAndPreservesBoth() throws Exception {
        Location parent = location("Parent");
        Location child = new Location();
        child.setName("Child");
        child.setParentLocation(parent);
        child = locations.saveAndFlush(child);
        mvc.perform(delete("/api/locations/{id}", parent.getId()).header("Authorization", token()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());
        assertTrue(locations.existsById(parent.getId()));
        assertTrue(locations.existsById(child.getId()));
    }

    @Test
    void rejectsDeletionWithNarration() throws Exception {
        Location location = location("Narrated");
        Language language = new Language(null, "qz", "Test language");
        language = languages.saveAndFlush(language);
        Narration narration = new Narration();
        narration.setLocation(location);
        narration.setLanguage(language);
        narration = narrations.saveAndFlush(narration);
        mvc.perform(delete("/api/locations/{id}", location.getId()).header("Authorization", token()))
                .andExpect(status().isConflict());
        assertTrue(locations.existsById(location.getId()));
        assertTrue(narrations.existsById(narration.getId()));
    }

    @Test
    void rejectsDeletionWithListeningHistory() throws Exception {
        Location location = location("Listened");
        PlaybackLog log = new PlaybackLog();
        log.setLocation(location);
        log.setLanguageCode("vi");
        log.setTriggerType(TriggerType.MANUAL);
        log = logs.saveAndFlush(log);
        mvc.perform(delete("/api/locations/{id}", location.getId()).header("Authorization", token()))
                .andExpect(status().isConflict());
        assertTrue(locations.existsById(location.getId()));
        assertTrue(logs.existsById(log.getId()));
    }

    @Test
    void deletesUnreferencedLocationAndReturns404WhenMissing() throws Exception {
        Location location = location("Unreferenced");
        mvc.perform(delete("/api/locations/{id}", location.getId()).header("Authorization", token()))
                .andExpect(status().isNoContent());
        assertFalse(locations.existsById(location.getId()));
        mvc.perform(delete("/api/locations/{id}", location.getId()).header("Authorization", token()))
                .andExpect(status().isNotFound());
    }
}
