package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDatabaseIsolationTest {
    @Test
    void overridesApplicationDatasourceBeforeContextStarts() {
        var context = new GenericApplicationContext();
        var environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/audiotour_db")
                .withProperty("spring.datasource.username", "root")
                .withProperty("AUDIOTOUR_TEST_DB_PASSWORD", "test-password");
        context.setEnvironment(environment);

        new TestDatabaseIsolation().initialize(context);

        assertEquals("jdbc:mysql://localhost:3306/audiotour_test?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC", environment.getProperty("spring.datasource.url"));
        assertEquals("audiotour_test", environment.getProperty("spring.datasource.username"));
        assertEquals("test-password", environment.getProperty("spring.datasource.password"));
    }

    @Test
    void prodProfileUsesSeparateDatabase() {
        var context = new GenericApplicationContext();
        var environment = new MockEnvironment().withProperty("AUDIOTOUR_TEST_DB_PASSWORD", "test-password");
        environment.setActiveProfiles("prod");
        context.setEnvironment(environment);

        new TestDatabaseIsolation().initialize(context);

        assertTrue(environment.getProperty("spring.datasource.url").contains("/audiotour_prod_profile_test?"));
    }

    @Test
    void refusesToStartWithoutTestPassword() {
        var context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());
        assertThrows(IllegalStateException.class, () -> new TestDatabaseIsolation().initialize(context));
    }
}
