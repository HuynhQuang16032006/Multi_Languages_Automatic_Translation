package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.service.LocationService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    void testGetRootLocations() {
        Location root = new Location();
        root.setId(1L);
        root.setName("Dinh Độc Lập");

        when(locationRepository.findByParentLocationIsNull()).thenReturn(List.of(root));

        List<Location> result = locationService.getRootLocations();

        assertEquals(1, result.size());
        assertEquals("Dinh Độc Lập", result.get(0).getName());
        verify(locationRepository, times(1)).findByParentLocationIsNull();
    }

    @Test
    void testGetLocationById_Found() {
        Location loc = new Location();
        loc.setId(1L);
        loc.setName("Dinh Độc Lập");

        when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));

        Location result = locationService.getLocationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetLocationById_NotFound() {
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> locationService.getLocationById(999L));
    }

    @Test
    void testGetSubLocations_Success() {
        Location parent = new Location();
        parent.setId(1L);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(parent));

        Location sub = new Location();
        sub.setId(2L);
        sub.setName("Phòng Khánh Tiết");

        when(locationRepository.findByParentLocationId(1L)).thenReturn(List.of(sub));

        List<Location> result = locationService.getSubLocations(1L);

        assertEquals(1, result.size());
        assertEquals("Phòng Khánh Tiết", result.get(0).getName());
    }

    @Test
    void testGetSubLocations_ParentNotFound() {
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> locationService.getSubLocations(999L));
    }
}
