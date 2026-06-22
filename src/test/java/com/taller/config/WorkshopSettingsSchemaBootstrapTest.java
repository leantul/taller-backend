package com.taller.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

class WorkshopSettingsSchemaBootstrapTest {

    @Test
    void run_addsAndInitializesReportTitleColumn() throws Exception {
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate();
        WorkshopSettingsSchemaBootstrap bootstrap = new WorkshopSettingsSchemaBootstrap(jdbcTemplate);

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        assertIterableEquals(List.of(
                """
                ALTER TABLE workshop_settings
                    ADD COLUMN IF NOT EXISTS report_title VARCHAR(200)
                """,
                """
                ALTER TABLE workshop_settings
                    ALTER COLUMN report_title SET NOT NULL
                """
        ), jdbcTemplate.executedSql);
        assertEquals("""
                UPDATE workshop_settings
                SET report_title = ?
                WHERE report_title IS NULL OR trim(report_title) = ''
                """, jdbcTemplate.updatedSql);
        assertEquals(List.of("REPORTE DE REPARACIÓN"), jdbcTemplate.updatedArgs);
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {
        private final List<String> executedSql = new ArrayList<>();
        private String updatedSql;
        private List<Object> updatedArgs = List.of();

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
        }

        @Override
        public int update(String sql, Object... args) {
            updatedSql = sql;
            updatedArgs = List.of(args);
            return 1;
        }
    }
}
