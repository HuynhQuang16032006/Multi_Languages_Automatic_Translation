package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import vn.edu.sgu.audiotour.controller.AnalyticsController;
import vn.edu.sgu.audiotour.controller.AudioPackageController;
import vn.edu.sgu.audiotour.controller.NarrationController;
import vn.edu.sgu.audiotour.dto.DeviceHeartbeatRequest;
import vn.edu.sgu.audiotour.dto.HeartbeatResponse;
import vn.edu.sgu.audiotour.dto.PlaybackLogRequest;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.service.AnalyticsService;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.NarrationService;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;
import vn.edu.sgu.audiotour.service.TtsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PublicLocationVisibilityTest {
    @Test
    void hiddenLocationCannotBeReadOrLoggedButCanLeave() {
        LocationService locations = mock(LocationService.class);
        NarrationService narrations = mock(NarrationService.class);
        LiveAudience audience = mock(LiveAudience.class);
        PlaybackLogRecorder recorder = mock(PlaybackLogRecorder.class);
        when(locations.getLocationById(1L)).thenThrow(new NotFoundException("hidden"));

        AudioPackageController packages = new AudioPackageController(narrations, locations);
        NarrationController narration = new NarrationController(narrations, mock(TtsService.class), locations);
        AnalyticsController analytics = new AnalyticsController(mock(AnalyticsService.class), audience, recorder, locations);

        assertThrows(NotFoundException.class, () -> packages.streamAudio(1L, "vi"));
        assertThrows(NotFoundException.class, () -> packages.downloadOfflinePackage(1L, "vi", null));
        assertThrows(NotFoundException.class, () -> narration.getNarration(1L, "vi"));
        PlaybackLogRequest log = new PlaybackLogRequest();
        log.setLocationId(1L);
        assertThrows(NotFoundException.class, () -> analytics.recordPlaybackLog(log));
        verifyNoInteractions(narrations, recorder);
    }
}
