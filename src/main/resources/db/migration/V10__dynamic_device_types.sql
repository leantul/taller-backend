CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS device_types (
    id_device_type VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_device_types_name
    ON device_types (LOWER(name));

INSERT INTO device_types (id_device_type, name, creation_date_time, modification_datetime)
SELECT gen_random_uuid()::text, seed.name, NOW(), NOW()
FROM (VALUES ('Desktop'), ('Notebook'), ('Tablet'), ('Celular'), ('Otros')) AS seed(name)
WHERE NOT EXISTS (
    SELECT 1 FROM device_types existing WHERE LOWER(existing.name) = LOWER(seed.name)
);

ALTER TABLE devices ADD COLUMN IF NOT EXISTS device_type_id VARCHAR(36);

UPDATE devices d
SET device_type_id = dt.id_device_type
FROM device_types dt
WHERE d.device_type_id IS NULL
  AND LOWER(dt.name) = LOWER(
      CASE d.device_type
          WHEN 'DESKTOP' THEN 'Desktop'
          WHEN 'NOTEBOOK' THEN 'Notebook'
          WHEN 'TABLET' THEN 'Tablet'
          WHEN 'CELULAR' THEN 'Celular'
          WHEN 'OTROS' THEN 'Otros'
      END
  );

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM devices WHERE device_type_id IS NULL) THEN
        RAISE EXCEPTION 'Existen dispositivos que no pudieron asociarse a un tipo';
    END IF;
END $$;

ALTER TABLE devices ALTER COLUMN device_type_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_devices_device_types'
    ) THEN
        ALTER TABLE devices
            ADD CONSTRAINT fk_devices_device_types
            FOREIGN KEY (device_type_id)
            REFERENCES device_types(id_device_type);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_devices_device_type_id ON devices(device_type_id);
