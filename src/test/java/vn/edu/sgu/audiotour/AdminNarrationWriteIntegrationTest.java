package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.security.JwtService;
import vn.edu.sgu.audiotour.repository.AdminAuthRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminNarrationWriteIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Autowired AdminAuthRepository admins;
    @Autowired LocationRepository locations;
    @Autowired LanguageRepository languages;
    @Autowired NarrationRepository narrations;
    @Autowired JdbcTemplate jdbc;

    @Test
    void managesTextAndPreservesExistingAudio() throws Exception {
        Location location = new Location();
        location.setName("Narration management");
        location = locations.saveAndFlush(location);
        Language language = languages.saveAndFlush(new Language(null, "qy", "Test"));
        String base = "/api/admin/locations/" + location.getId() + "/narrations";
        String token = "Bearer " + jwt.generateToken("admin", admins.findById(1).orElseThrow().getPasswordHash());
        String body = "{\"languageCode\":\"qy\",\"textContent\":\"Nội dung cũ\"}";
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post(base).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post(base).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        Narration narration = narrations.findByLocationIdAndLanguageCode(location.getId(), language.getCode()).orElseThrow();
        narration.setAudioUrl("shared-audio.mp3");
        narration.setDurationInSeconds(30);
        narrations.saveAndFlush(narration);
        mvc.perform(put(base + "/" + narration.getId()).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"languageCode\":\"qy\",\"textContent\":\"Nội dung mới\"}"))
                .andExpect(status().isOk());
        narration = narrations.findById(narration.getId()).orElseThrow();
        assertEquals("Nội dung mới", narration.getTextContent());
        assertEquals("shared-audio.mp3", narration.getAudioUrl());
        assertEquals(30, narration.getDurationInSeconds());
        mvc.perform(delete(base + "/" + narration.getId()).header("Authorization", token)).andExpect(status().isNoContent());
        assertFalse(narrations.existsById(narration.getId()));
    }
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void databaseRejectsConcurrentDuplicatesAndNullKeys() throws Exception {
        Location location = new Location();
        location.setName("Concurrent narration");
        location = locations.saveAndFlush(location);
        Language language = languages.saveAndFlush(new Language(null, UUID.randomUUID().toString().substring(0, 8), "Test"));
        long locationId = location.getId();
        long languageId = language.getId();
        var executor = Executors.newFixedThreadPool(2);
        try {
            var barrier = new CyclicBarrier(2);
            var insert = (java.util.concurrent.Callable<Boolean>) () -> {
                barrier.await(10, TimeUnit.SECONDS);
                try {
                    jdbc.update("INSERT INTO narrations (location_id, language_id) VALUES (?, ?)", locationId, languageId);
                    return true;
                } catch (DataIntegrityViolationException ex) {
                    assertTrue(ex.getMostSpecificCause() instanceof java.sql.SQLException);
                    assertEquals(1062, ((java.sql.SQLException) ex.getMostSpecificCause()).getErrorCode());
                    return false;
                }
            };
            var first = executor.submit(insert);
            var second = executor.submit(insert);
            assertNotEquals(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM narrations WHERE location_id = ? AND language_id = ?", Integer.class, locationId, languageId));
            assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO narrations (location_id, language_id) VALUES (NULL, ?)", languageId));
            assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO narrations (location_id, language_id) VALUES (?, NULL)", locationId));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(20, TimeUnit.SECONDS);
            jdbc.update("DELETE FROM narrations WHERE location_id = ? AND language_id = ?", locationId, languageId);
            languages.deleteById(languageId);
            locations.deleteById(locationId);
        }
    }
}
