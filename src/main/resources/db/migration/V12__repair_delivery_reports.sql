CREATE TABLE IF NOT EXISTS workshop_settings (
    id_workshop_settings VARCHAR(36) PRIMARY KEY,
    business_name VARCHAR(160) NOT NULL,
    whatsapp VARCHAR(80),
    instagram VARCHAR(160),
    logo_asset_path VARCHAR(255) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS software_catalog_items (
    id_software_catalog_item VARCHAR(36) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    detail VARCHAR(255),
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_software_catalog_items_name
    ON software_catalog_items (LOWER(name));

CREATE TABLE IF NOT EXISTS repair_reports (
    id_repair_report VARCHAR(36) PRIMARY KEY,
    repair_id VARCHAR(36) NOT NULL,
    order_number VARCHAR(50),
    issued_at TIMESTAMP NOT NULL,
    client_name VARCHAR(120),
    client_last_name VARCHAR(120),
    client_phone VARCHAR(80),
    client_email VARCHAR(160),
    client_dni VARCHAR(40),
    device_type_name VARCHAR(120),
    device_brand VARCHAR(120),
    device_model VARCHAR(120),
    device_serial_number VARCHAR(120),
    reported_issue TEXT,
    work_performed TEXT,
    final_observations TEXT,
    show_part_prices BOOLEAN NOT NULL DEFAULT FALSE,
    final_amount NUMERIC(12,2),
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repair_reports_repairs FOREIGN KEY (repair_id) REFERENCES repairs(id_repair)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_repair_reports_repair_id ON repair_reports(repair_id);

CREATE TABLE IF NOT EXISTS repair_report_hardware_items (
    id_repair_report_hardware_item VARCHAR(36) PRIMARY KEY,
    repair_report_id VARCHAR(36) NOT NULL,
    part_name VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    detail VARCHAR(255),
    unit_price NUMERIC(12,2),
    include_price BOOLEAN NOT NULL DEFAULT TRUE,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repair_report_hardware_items_reports
        FOREIGN KEY (repair_report_id) REFERENCES repair_reports(id_repair_report)
);

CREATE INDEX IF NOT EXISTS idx_repair_report_hardware_items_report_id
    ON repair_report_hardware_items(repair_report_id);

CREATE TABLE IF NOT EXISTS repair_report_software_items (
    id_repair_report_software_item VARCHAR(36) PRIMARY KEY,
    repair_report_id VARCHAR(36) NOT NULL,
    software_name VARCHAR(160) NOT NULL,
    detail VARCHAR(255),
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repair_report_software_items_reports
        FOREIGN KEY (repair_report_id) REFERENCES repair_reports(id_repair_report)
);

CREATE INDEX IF NOT EXISTS idx_repair_report_software_items_report_id
    ON repair_report_software_items(repair_report_id);
