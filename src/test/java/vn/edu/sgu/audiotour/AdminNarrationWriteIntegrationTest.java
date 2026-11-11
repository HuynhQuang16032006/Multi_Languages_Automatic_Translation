package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
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
}
