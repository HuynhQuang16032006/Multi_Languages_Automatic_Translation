package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDatabaseIsolationTest {
    @Test
    void overridesApplicationDatasourceBeforeContextStarts() {
        var context = new GenericApplicationContext();
        var environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/audiotour_db")
                .withProperty("spring.datasource.hikari.jdbc-url", "jdbc:mysql://localhost:3306/audiotour_db")
                .withProperty("spring.datasource.hikari.username", "root")
                .withProperty("spring.datasource.hikari.password", "application-password")
                .withProperty("spring.datasource.username", "root")
                .withProperty("spring.flyway.url", "jdbc:mysql://localhost:3306/audiotour_db")
                .withProperty("spring.flyway.user", "root")
                .withProperty("spring.flyway.password", "application-password")
                .withProperty("AUDIOTOUR_TEST_DB_PASSWORD", "test-password");
        context.setEnvironment(environment);

        new TestDatabaseIsolation().initialize(context);

        assertEquals("jdbc:mysql://127.0.0.1:33316/audiotour_test?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC", environment.getProperty("spring.datasource.url"));
        assertEquals(environment.getProperty("spring.datasource.url"), environment.getProperty("spring.datasource.hikari.jdbc-url"));
        assertEquals("audiotour_test", environment.getProperty("spring.datasource.username"));
        assertEquals("test-password", environment.getProperty("spring.datasource.password"));
        assertEquals("audiotour_test", environment.getProperty("spring.datasource.hikari.username"));
        assertEquals("test-password", environment.getProperty("spring.datasource.hikari.password"));
        assertEquals(environment.getProperty("spring.datasource.url"), environment.getProperty("spring.flyway.url"));
        assertEquals("audiotour_test", environment.getProperty("spring.flyway.user"));
        assertEquals("test-password", environment.getProperty("spring.flyway.password"));
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

    @Test
    void refusesBlankTestPassword() {
        var context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment().withProperty("AUDIOTOUR_TEST_DB_PASSWORD", "  "));
        assertThrows(IllegalStateException.class, () -> new TestDatabaseIsolation().initialize(context));
    }

    @Test
    void springDiscoversCustomizerAndFailsClosedWithoutPassword() {
        var factories = SpringFactoriesLoader.loadFactories(ContextCustomizerFactory.class, getClass().getClassLoader());
        var factory = factories.stream().filter(TestDatabaseIsolation.class::isInstance).findFirst().orElseThrow();
        var context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());

        var customizer = factory.createContextCustomizer(ProdProfileTest.class, List.of());
        assertThrows(IllegalStateException.class, () -> customizer.customizeContext(context, null));
    }
}
