package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.security.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminNarrationListIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Autowired LocationRepository locations;
    @Autowired LanguageRepository languages;
    @Autowired NarrationRepository narrations;

    @Test
    void listsNarrationsForLocationOnlyForAdmin() throws Exception {
        Location location = new Location();
        location.setName("Admin narration test");
        location = locations.saveAndFlush(location);
        Language language = languages.saveAndFlush(new Language(null, "qx", "Test"));
        Narration narration = new Narration();
        narration.setLocation(location);
        narration.setLanguage(language);
        narration.setTextContent("Test content");
        narrations.saveAndFlush(narration);
        String url = "/api/admin/locations/" + location.getId() + "/narrations";
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
        mvc.perform(get(url).header("Authorization", "Bearer " + jwt.generateToken("admin")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].textContent").value("Test content"));
    }
}
