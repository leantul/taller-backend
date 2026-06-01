CREATE TABLE IF NOT EXISTS device_observations (
    id_device_observation VARCHAR(36) PRIMARY KEY,
    device_id VARCHAR(36) NOT NULL,
    repair_id VARCHAR(36),
    note TEXT NOT NULL,
    observed_at TIMESTAMP NOT NULL,
    follow_up_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_device_observations_devices FOREIGN KEY (device_id) REFERENCES devices(id_device),
    CONSTRAINT fk_device_observations_repairs FOREIGN KEY (repair_id) REFERENCES repairs(id_repair)
);

CREATE INDEX IF NOT EXISTS idx_device_observations_device_id ON device_observations(device_id);
CREATE INDEX IF NOT EXISTS idx_device_observations_repair_id ON device_observations(repair_id);
CREATE INDEX IF NOT EXISTS idx_device_observations_follow_up_at ON device_observations(follow_up_at);
CREATE INDEX IF NOT EXISTS idx_device_observations_resolved_at ON device_observations(resolved_at);
CREATE INDEX IF NOT EXISTS idx_device_observations_pending_follow_up
    ON device_observations(resolved_at, follow_up_at);
