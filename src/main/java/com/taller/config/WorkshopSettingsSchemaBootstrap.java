package com.taller.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class WorkshopSettingsSchemaBootstrap implements ApplicationRunner {

    private static final String DEFAULT_REPORT_TITLE = "REPORTE DE REPARACIÓN";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                ALTER TABLE workshop_settings
                    ADD COLUMN IF NOT EXISTS report_title VARCHAR(200)
                """);

        jdbcTemplate.update("""
                UPDATE workshop_settings
                SET report_title = ?
                WHERE report_title IS NULL OR trim(report_title) = ''
                """, DEFAULT_REPORT_TITLE);

        jdbcTemplate.execute("""
                ALTER TABLE workshop_settings
                    ALTER COLUMN report_title SET NOT NULL
                """);
    }
}
