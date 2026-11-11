package vn.edu.sgu.audiotour;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;
import java.util.Map;

public class TestDatabaseIsolation implements ApplicationContextInitializer<ConfigurableApplicationContext>, ContextCustomizerFactory {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var environment = context.getEnvironment();
        var password = environment.getProperty("AUDIOTOUR_TEST_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("AUDIOTOUR_TEST_DB_PASSWORD is required for isolated tests");
        }
        var database = List.of(environment.getActiveProfiles()).contains("prod")
                ? "audiotour_prod_profile_test" : "audiotour_test";
        var url = "jdbc:mysql://127.0.0.1:33316/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC";
        environment.getPropertySources().addFirst(new MapPropertySource("isolatedTestDatabase", Map.of(
                "spring.datasource.url", url,
                "spring.datasource.hikari.jdbc-url", url,
                "spring.datasource.username", "audiotour_test",
                "spring.datasource.password", password,
                "spring.datasource.hikari.username", "audiotour_test",
                "spring.datasource.hikari.password", password,
                "spring.flyway.url", url,
                "spring.flyway.user", "audiotour_test",
                "spring.flyway.password", password)));
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        return (context, mergedConfig) -> initialize(context);
    }
}
