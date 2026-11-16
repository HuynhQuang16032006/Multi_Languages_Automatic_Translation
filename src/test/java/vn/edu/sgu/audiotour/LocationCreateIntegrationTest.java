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
import vn.edu.sgu.audiotour.repository.AdminAuthRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LocationCreateIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwt;
    @Autowired private AdminAuthRepository admins;
    @Autowired private LocationRepository locations;

    private String token() { return "Bearer " + jwt.generateToken("admin", admins.findById(1).orElseThrow().getPasswordHash()); }

    @Test
    void createsLocationAndRejectsDuplicateQr() throws Exception {
        String body = "{\"name\":\"Địa điểm mới\",\"latitude\":10.5,\"longitude\":106.5,\"radiusInMeters\":30,\"priority\":1,\"qrCode\":\"TEST-NEW-QR\"}";
        mvc.perform(post("/api/locations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/locations").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Địa điểm mới"));
        mvc.perform(post("/api/locations").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidFieldsAndMissingParent() throws Exception {
        mvc.perform(post("/api/locations").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"latitude\":91,\"radiusInMeters\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/locations").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Orphan\",\"parentId\":999999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsChildOfExistingLocation() throws Exception {
        Location parent = new Location();
        parent.setName("Parent for creation");
        parent = locations.saveAndFlush(parent);
        mvc.perform(post("/api/locations").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Child\",\"parentId\":" + parent.getId() + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.parentId").value(parent.getId()));
    }
}
