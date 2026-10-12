package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.sgu.audiotour.controller.LocationController;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.service.LocationService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    @Test
    void testGetRootLocations() throws Exception {
        Location loc = new Location();
        loc.setId(1L);
        loc.setName("Dinh Độc Lập");

        when(locationService.getRootLocations()).thenReturn(List.of(loc));

        mockMvc.perform(get("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Dinh Độc Lập"));
    }

    @Test
    void testGetLocationById() throws Exception {
        Location loc = new Location();
        loc.setId(1L);
        loc.setName("Dinh Độc Lập");

        when(locationService.getLocationById(1L)).thenReturn(loc);

        mockMvc.perform(get("/api/locations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Dinh Độc Lập"));
    }

    @Test
    void testGetSubLocations() throws Exception {
        Location sub = new Location();
        sub.setId(2L);
        sub.setName("Phòng Khánh Tiết");

        when(locationService.getSubLocations(1L)).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/locations/1/sub-locations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].name").value("Phòng Khánh Tiết"));
    }
}
