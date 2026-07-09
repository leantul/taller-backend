CREATE TABLE IF NOT EXISTS repair_status_history (
    id_repair_status_history VARCHAR(36) PRIMARY KEY,
    repair_id VARCHAR(36) NOT NULL,
    status INTEGER NOT NULL,
    changed_at TIMESTAMP,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repair_status_history_repairs FOREIGN KEY (repair_id) REFERENCES repairs(id_repair)
);

CREATE INDEX IF NOT EXISTS idx_repair_status_history_repair_changed
    ON repair_status_history(repair_id, changed_at ASC);

INSERT INTO repair_status_history (
    id_repair_status_history,
    repair_id,
    status,
    changed_at,
    creation_date_time,
    modification_datetime
)
SELECT
    gen_random_uuid()::text,
    r.id_repair,
    2,
    r.receive_date_time,
    NOW(),
    NOW()
FROM repairs r
WHERE r.receive_date_time IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM repair_status_history h
      WHERE h.repair_id = r.id_repair
        AND h.status = 2
        AND h.changed_at = r.receive_date_time
  );

INSERT INTO repair_status_history (
    id_repair_status_history,
    repair_id,
    status,
    changed_at,
    creation_date_time,
    modification_datetime
)
SELECT
    gen_random_uuid()::text,
    r.id_repair,
    6,
    r.return_date_time,
    NOW(),
    NOW()
FROM repairs r
WHERE r.return_date_time IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM repair_status_history h
      WHERE h.repair_id = r.id_repair
        AND h.status = 6
        AND h.changed_at = r.return_date_time
  );
