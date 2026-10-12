package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.entity.Tour;
import vn.edu.sgu.audiotour.repository.TourRepository;
import vn.edu.sgu.audiotour.service.TourService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private TourService tourService;

    @Test
    void testGetAllTours() {
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setName("Tour Tuyến Xe Buýt Quận 4");

        when(tourRepository.findAll()).thenReturn(List.of(tour));

        List<Tour> result = tourService.getAllTours();

        assertEquals(1, result.size());
        assertEquals("Tour Tuyến Xe Buýt Quận 4", result.get(0).getName());
    }

    @Test
    void testGetTourById_Found() {
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setName("Tour Tuyến Xe Buýt Quận 4");

        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        Tour result = tourService.getTourById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetTourById_NotFound() {
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tourService.getTourById(999L));
    }
}
