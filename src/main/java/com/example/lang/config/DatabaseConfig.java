package com.example.lang.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            // Fallback для локальной разработки
            databaseUrl = "jdbc:postgresql://localhost:5432/lang?user=postgres&password=postgres";
        } else {
            // Конвертируем postgres:// в jdbc:postgresql://
            databaseUrl = databaseUrl.replaceFirst("^postgres://", "jdbc:postgresql://");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseUrl);

        // Парсим username и password из URL
        try {
            String withoutProtocol = databaseUrl.substring("jdbc:postgresql://".length());
            String[] atParts = withoutProtocol.split("@", 2);

            if (atParts.length == 2) {
                String credentials = atParts[0];
                String[] credParts = credentials.split(":", 2);

                if (credParts.length >= 2) {
                    dataSource.setUsername(credParts[0]);
                    String passAndHost = credParts[1];
                    int slashIndex = passAndHost.indexOf('/');

                    if (slashIndex > 0) {
                        dataSource.setPassword(passAndHost.substring(0, slashIndex));
                    } else {
                        dataSource.setPassword(passAndHost);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not parse DB credentials: " + e.getMessage());
        }

        // Настройки пула соединений для Neon
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }
}