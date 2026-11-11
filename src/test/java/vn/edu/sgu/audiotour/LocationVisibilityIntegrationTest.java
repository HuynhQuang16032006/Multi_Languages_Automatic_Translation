package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.entity.PlaybackLog;
import vn.edu.sgu.audiotour.entity.TriggerType;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
import vn.edu.sgu.audiotour.security.JwtService;
import vn.edu.sgu.audiotour.repository.AdminAuthRepository;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocationVisibilityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired LocationRepository locations;
    @Autowired PlaybackLogRepository logs;
    @Autowired JwtService jwt;
    @Autowired AdminAuthRepository admins;

    private Location create(String name, Location parent, String qr) {
        Location location = new Location();
        location.setName(name);
        location.setParentLocation(parent);
        location.setQrCode(qr);
        return locations.saveAndFlush(location);
    }

    private String token() { return "Bearer " + jwt.generateToken("admin", admins.findById(1).orElseThrow().getPasswordHash()); }

    @Test
    void hidingPreservesDataAndFiltersPublicResponses() throws Exception {
        Location root = create("Visible root", null, null);
        Location child = create("Hidden child", root, "HIDDEN-QR");
        PlaybackLog log = new PlaybackLog();
        log.setLocation(child);
        log.setLanguageCode("vi");
        log.setTriggerType(TriggerType.MANUAL);
        log = logs.saveAndFlush(log);
        mvc.perform(patch("/api/admin/locations/{id}/visibility", child.getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":false}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/admin/locations/{id}/visibility", child.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.visible").value(false));
        mvc.perform(get("/api/locations/{id}", child.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/locations/by-qr/HIDDEN-QR")).andExpect(status().isNotFound());
        mvc.perform(get("/api/locations/{id}", root.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.subLocations[*].id", not(hasItem(child.getId().intValue()))));
        mvc.perform(get("/api/locations/pois")).andExpect(jsonPath("$[*].id", not(hasItem(child.getId().intValue()))));
        mvc.perform(get("/api/v1/locations")).andExpect(jsonPath("$.content[*].id", not(hasItem(child.getId().intValue()))));
        mvc.perform(get("/api/locations/{id}/sub-locations", root.getId()))
                .andExpect(jsonPath("$[*].id", not(hasItem(child.getId().intValue()))));
        mvc.perform(get("/api/admin/locations").header("Authorization", token()))
                .andExpect(jsonPath("$[*].id", hasItem(child.getId().intValue())));
        assertEquals("HIDDEN-QR", locations.findById(child.getId()).orElseThrow().getQrCode());
        assertTrue(logs.existsById(log.getId()));
        mvc.perform(patch("/api/admin/locations/{id}/visibility", child.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":true}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/locations/{id}", child.getId())).andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidVisibilityTransitionsAndQrButPreservesLegacyQr() throws Exception {
        Location root = create("Root", null, "legacy qr");
        Location child = create("Child", root, null);
        mvc.perform(patch("/api/admin/locations/{id}/visibility", root.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":false}")).andExpect(status().isConflict());
        mvc.perform(put("/api/locations/{id}", root.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Root\",\"qrCode\":\"legacy qr\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/locations/{id}", root.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Root\",\"qrCode\":\"new invalid\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/locations").header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New\",\"qrCode\":\"invalid qr\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/admin/locations/{id}/visibility", child.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":false}")).andExpect(status().isOk());
        mvc.perform(patch("/api/admin/locations/{id}/visibility", root.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":false}")).andExpect(status().isOk());
        mvc.perform(patch("/api/admin/locations/{id}/visibility", child.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{\"visible\":true}")).andExpect(status().isConflict());
        mvc.perform(patch("/api/admin/locations/{id}/visibility", child.getId()).header("Authorization", token())
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }
}
