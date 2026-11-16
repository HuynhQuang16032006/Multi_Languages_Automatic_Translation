package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.audiotour.controller.AdminNarrationController;
import vn.edu.sgu.audiotour.controller.AnalyticsController;
import vn.edu.sgu.audiotour.controller.AudioPackageController;
import vn.edu.sgu.audiotour.controller.AuthController;
import vn.edu.sgu.audiotour.controller.LanguageController;
import vn.edu.sgu.audiotour.controller.LocationController;
import vn.edu.sgu.audiotour.controller.NarrationController;
import vn.edu.sgu.audiotour.repository.AdminAuthRepository;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.repository.PackageDownloadRepository;
import vn.edu.sgu.audiotour.repository.PlaybackLogRepository;
import vn.edu.sgu.audiotour.service.AdminNarrationService;
import vn.edu.sgu.audiotour.service.AnalyticsService;
import vn.edu.sgu.audiotour.service.AuthService;
import vn.edu.sgu.audiotour.service.LanguageOnboardingService;
import vn.edu.sgu.audiotour.service.LanguageService;
import vn.edu.sgu.audiotour.service.LiveAudience;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.NarrationService;
import vn.edu.sgu.audiotour.service.PlaybackLogRecorder;
import vn.edu.sgu.audiotour.service.TranslationService;
import vn.edu.sgu.audiotour.service.TtsService;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử tự động xác nhận chuẩn Kiến trúc 3 Lớp (3-Tier Architecture):
 * Presentation Tier (Controller) -> Business Logic Tier (Service) -> Data Access Tier (Repository).
 */
class Architecture3TierTest {

    private static final List<Class<?>> CORE_CONTROLLERS = List.of(
            LocationController.class,
            NarrationController.class,
            AdminNarrationController.class,
            AudioPackageController.class,
            AnalyticsController.class,
            LanguageController.class,
            AuthController.class
    );

    private static final List<Class<?>> BUSINESS_SERVICES = List.of(
            LocationService.class,
            NarrationService.class,
            AdminNarrationService.class,
            AnalyticsService.class,
            LiveAudience.class,
            PlaybackLogRecorder.class,
            LanguageService.class,
            LanguageOnboardingService.class,
            TranslationService.class,
            TtsService.class,
            AuthService.class
    );

    private static final List<Class<?>> DATA_REPOSITORIES = List.of(
            LocationRepository.class,
            LanguageRepository.class,
            NarrationRepository.class,
            PlaybackLogRepository.class,
            PackageDownloadRepository.class,
            AdminAuthRepository.class
    );

    @Test
    @DisplayName("Tầng 1 (Presentation): Các REST Controller phải được đánh dấu @RestController và thuộc gói controller")
    void presentationTierControllersShouldBeAnnotatedAndInControllerPackage() {
        for (Class<?> controller : CORE_CONTROLLERS) {
            assertThat(controller.isAnnotationPresent(RestController.class))
                    .as("%s phải có @RestController", controller.getSimpleName())
                    .isTrue();
            assertThat(controller.getPackageName())
                    .isEqualTo("vn.edu.sgu.audiotour.controller");
        }
        assertThat(AdminNarrationController.class.isAnnotationPresent(RestController.class)).isTrue();
    }

    @Test
    @DisplayName("Tầng 1 -> Tầng 2: Các Core Controller chỉ giao tiếp qua Service, không phụ thuộc trực tiếp vào Repository")
    void coreControllersShouldNotInjectRepositoriesDirectly() {
        for (Class<?> controller : CORE_CONTROLLERS) {
            for (Field field : controller.getDeclaredFields()) {
                assertThat(JpaRepository.class.isAssignableFrom(field.getType()))
                        .as("Controller %s không được inject trực tiếp Repository (%s)",
                                controller.getSimpleName(), field.getType().getSimpleName())
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("Tầng 2 (Business Logic): Các lớp nghiệp vụ phải được đánh dấu @Service và thuộc gói service")
    void businessTierServicesShouldBeAnnotatedAndInServicePackage() {
        for (Class<?> service : BUSINESS_SERVICES) {
            assertThat(service.isAnnotationPresent(Service.class))
                    .as("%s phải có @Service", service.getSimpleName())
                    .isTrue();
            assertThat(service.getPackageName())
                    .isEqualTo("vn.edu.sgu.audiotour.service");
        }
    }

    @Test
    @DisplayName("Tầng 3 (Data Access): Các Repository phải kế thừa JpaRepository và thuộc gói repository")
    void dataAccessTierRepositoriesShouldExtendJpaRepository() {
        for (Class<?> repo : DATA_REPOSITORIES) {
            assertThat(JpaRepository.class.isAssignableFrom(repo))
                    .as("%s phải kế thừa JpaRepository", repo.getSimpleName())
                    .isTrue();
            assertThat(repo.getPackageName())
                    .isEqualTo("vn.edu.sgu.audiotour.repository");
        }
    }
}
