ALTER TABLE clients ADD COLUMN IF NOT EXISTS last_name VARCHAR(120);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS dni VARCHAR(30);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS email VARCHAR(120);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE TABLE IF NOT EXISTS client_phones (
    client_id VARCHAR(36) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    CONSTRAINT fk_client_phones_clients FOREIGN KEY (client_id) REFERENCES clients(id_client)
);

CREATE TABLE IF NOT EXISTS client_emails (
    client_id VARCHAR(36) NOT NULL,
    email VARCHAR(120) NOT NULL,
    CONSTRAINT fk_client_emails_clients FOREIGN KEY (client_id) REFERENCES clients(id_client)
);

ALTER TABLE devices ADD COLUMN IF NOT EXISTS device_type VARCHAR(20);
ALTER TABLE devices ADD COLUMN IF NOT EXISTS password VARCHAR(120);
ALTER TABLE devices ADD COLUMN IF NOT EXISTS accessories TEXT;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS aesthetic_condition VARCHAR(120);

ALTER TABLE repairs ADD COLUMN IF NOT EXISTS order_number VARCHAR(50);
ALTER TABLE repairs ADD COLUMN IF NOT EXISTS labor_amount NUMERIC(12,2);
ALTER TABLE repairs ADD COLUMN IF NOT EXISTS extra_amount NUMERIC(12,2);
ALTER TABLE repairs ADD COLUMN IF NOT EXISTS quoted_amount NUMERIC(12,2);
ALTER TABLE repairs ADD COLUMN IF NOT EXISTS approved BOOLEAN;
ALTER TABLE repairs ADD COLUMN IF NOT EXISTS rejected BOOLEAN;
ALTER TABLE repairs ADD COLUMN IF NOT EXISTS ready_notified_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS repair_parts (
    id_repair_part VARCHAR(36) PRIMARY KEY,
    repair_id VARCHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    quantity INTEGER,
    provider VARCHAR(120),
    cost NUMERIC(12,2),
    sale_price NUMERIC(12,2),
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repair_parts_repairs FOREIGN KEY (repair_id) REFERENCES repairs(id_repair)
);

CREATE TABLE IF NOT EXISTS repair_payments (
    id_repair_payment VARCHAR(36) PRIMARY KEY,
    repair_id VARCHAR(36) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_date TIMESTAMP,
    notes TEXT,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repair_payments_repairs FOREIGN KEY (repair_id) REFERENCES repairs(id_repair)
);

CREATE TABLE IF NOT EXISTS notifications (
    id_notification VARCHAR(36) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT,
    readed BOOLEAN NOT NULL DEFAULT FALSE,
    event_date TIMESTAMP,
    type VARCHAR(60),
    entity_id VARCHAR(36),
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_repairs_order_number ON repairs(order_number);
CREATE INDEX IF NOT EXISTS idx_notifications_event_date ON notifications(event_date);
