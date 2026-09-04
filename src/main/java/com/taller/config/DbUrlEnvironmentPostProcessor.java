package com.taller.config;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DbUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dbUrlNormalizer";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String dbUrl = environment.getProperty("DB_URL");

        if (dbUrl == null || dbUrl.isBlank() || dbUrl.startsWith("jdbc:")) {
            return;
        }

        if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
            String jdbcUrl = toJdbcPostgresUrl(dbUrl);
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("spring.datasource.url", jdbcUrl);
            String userInfo = URI.create(dbUrl).getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] credentials = userInfo.split(":", 2);
                if (credentials.length > 0 && !credentials[0].isBlank() && environment.getProperty("DB_USERNAME") == null) {
                    properties.put("spring.datasource.username", credentials[0]);
                }
                if (credentials.length > 1 && !credentials[1].isBlank() && environment.getProperty("DB_PASSWORD") == null) {
                    properties.put("spring.datasource.password", credentials[1]);
                }
            }
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private String toJdbcPostgresUrl(String rawUrl) {
        URI uri = URI.create(rawUrl);

        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null ? "" : uri.getPath();
        String query = uri.getQuery();

        StringBuilder builder = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(":")
                .append(port)
                .append(path);

        if (query != null && !query.isBlank()) {
            builder.append("?").append(query);
        }

        return builder.toString();
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
