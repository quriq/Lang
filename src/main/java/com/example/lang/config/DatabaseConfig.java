package com.example.lang.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();

        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Просто конвертируем протокол, имя БД берём из URL
            String jdbcUrl = databaseUrl.replaceFirst("^postgres://", "jdbc:postgresql://");
            properties.setUrl(jdbcUrl);

            // Парсим username и password
            try {
                String withoutProtocol = jdbcUrl.substring("jdbc:postgresql://".length());
                String[] atParts = withoutProtocol.split("@", 2);

                if (atParts.length == 2) {
                    String credentials = atParts[0];
                    String[] credParts = credentials.split(":", 2);

                    if (credParts.length >= 2) {
                        properties.setUsername(credParts[0]);
                        String passAndHost = credParts[1];
                        int slashIndex = passAndHost.indexOf('/');

                        if (slashIndex > 0) {
                            properties.setPassword(passAndHost.substring(0, slashIndex));
                        } else {
                            properties.setPassword(passAndHost);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not parse DB credentials: " + e.getMessage());
            }
        } else {
            // Локальная разработка
            properties.setUrl("jdbc:postgresql://localhost:5432/lang");
            properties.setUsername("postgres");
            properties.setPassword("postgres");
        }

        return properties;
    }
}