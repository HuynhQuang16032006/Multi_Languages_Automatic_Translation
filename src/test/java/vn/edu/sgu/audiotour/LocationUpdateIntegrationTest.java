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
import vn.edu.sgu.audiotour.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocationUpdateIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired LocationRepository locations;
    @Autowired JwtService jwt;

    private Location create(String name, Location parent, String qr) {
        Location location = new Location();
        location.setName(name);
        location.setParentLocation(parent);
        location.setQrCode(qr);
        return locations.saveAndFlush(location);
    }

    private void update(long id, String body, int status) throws Exception {
        mvc.perform(put("/api/locations/{id}", id).header("Authorization", "Bearer " + jwt.generateToken("admin"))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().is(status));
    }

    @Test
    void updatesAndRejectsInvalidRequests() throws Exception {
        Location one = create("Original", null, "UPDATE-QR-1");
        create("Other", null, "UPDATE-QR-2");
        mvc.perform(put("/api/locations/{id}", one.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Changed\"}")).andExpect(status().isUnauthorized());
        update(one.getId(), "{\"name\":\"Changed\",\"qrCode\":\"UPDATE-QR-1\"}", 200);
        assertEquals("Changed", locations.findById(one.getId()).orElseThrow().getName());
        update(one.getId(), "{\"name\":\"Changed\",\"qrCode\":\"UPDATE-QR-2\"}", 409);
        update(one.getId(), "{\"name\":\"\",\"longitude\":181}", 400);
        update(999999999L, "{\"name\":\"Missing\"}", 404);
    }

    @Test
    void rejectsSelfAndDescendantParent() throws Exception {
        Location root = create("Root", null, null);
        Location child = create("Child", root, null);
        Location grandchild = create("Grandchild", child, null);
        update(root.getId(), "{\"name\":\"Root\",\"parentId\":" + root.getId() + "}", 409);
        update(root.getId(), "{\"name\":\"Root\",\"parentId\":" + grandchild.getId() + "}", 409);
        assertNull(locations.findById(root.getId()).orElseThrow().getParentLocation());
    }
}
