ALTER TABLE notifications ADD COLUMN IF NOT EXISTS repair_id VARCHAR(36);

CREATE TABLE IF NOT EXISTS app_metadata (
    metadata_key VARCHAR(120) PRIMARY KEY,
    metadata_value TEXT
);

CREATE INDEX IF NOT EXISTS idx_notifications_repair_id ON notifications(repair_id);
CREATE INDEX IF NOT EXISTS idx_notifications_entity_type_event
    ON notifications(entity_id, type, event_date);
