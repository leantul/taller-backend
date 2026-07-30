package com.taller.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresSearchIndexBootstrap implements ApplicationRunner {

    private static final List<String> SEARCH_INDEXES = List.of(
            "CREATE INDEX IF NOT EXISTS idx_clients_name_trgm ON clients USING gin (lower(name) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_clients_last_name_trgm ON clients USING gin (lower(last_name) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_clients_reference_trgm ON clients USING gin (lower(reference) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_clients_phone_trgm ON clients USING gin (lower(phone) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_clients_email_trgm ON clients USING gin (lower(email) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_devices_brand_trgm ON devices USING gin (lower(brand) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_devices_model_trgm ON devices USING gin (lower(model) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_devices_serial_number_trgm ON devices USING gin (lower(serial_number) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_repairs_order_number_trgm ON repairs USING gin (lower(order_number) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_repairs_description_trgm ON repairs USING gin (lower(description) gin_trgm_ops)"
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureRepairOrderSequence();
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
            SEARCH_INDEXES.forEach(jdbcTemplate::execute);
        } catch (DataAccessException exception) {
            // Some managed PostgreSQL plans do not allow installing extensions. The application can
            // continue with identical search behaviour, only without the optional acceleration.
            log.warn("Could not ensure PostgreSQL trigram search indexes; continuing without them: {}",
                    exception.getMostSpecificCause().getMessage());
        }
    }

    private void ensureRepairOrderSequence() {
        Boolean sequenceExists = jdbcTemplate.queryForObject(
                "SELECT to_regclass('repair_order_seq') IS NOT NULL", Boolean.class);
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS repair_order_seq START WITH 1 INCREMENT BY 1");
        if (Boolean.TRUE.equals(sequenceExists)) {
            return;
        }
        Long maximumOrder = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(CAST(order_number AS BIGINT)), 0)
                FROM repairs
                WHERE order_number ~ '^[0-9]+$'
                """, Long.class);
        long value = maximumOrder != null ? maximumOrder : 0L;
        jdbcTemplate.queryForObject(
                "SELECT setval('repair_order_seq', ?, ?)", Long.class,
                value > 0 ? value : 1L,
                value > 0);
    }
}
