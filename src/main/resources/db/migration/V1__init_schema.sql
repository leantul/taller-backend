CREATE TABLE IF NOT EXISTS clients (
    id_client VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(40),
    birth_date DATE,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS devices (
    id_device VARCHAR(36) PRIMARY KEY,
    brand VARCHAR(120) NOT NULL,
    model VARCHAR(120) NOT NULL,
    serial_number VARCHAR(120),
    client_id VARCHAR(36) NOT NULL,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_devices_clients FOREIGN KEY (client_id) REFERENCES clients(id_client)
);

CREATE TABLE IF NOT EXISTS repairs (
    id_repair VARCHAR(36) PRIMARY KEY,
    id_device VARCHAR(36) NOT NULL,
    id_client VARCHAR(36) NOT NULL,
    description TEXT,
    status INTEGER NOT NULL,
    receive_date_time TIMESTAMP,
    return_date_time TIMESTAMP,
    price NUMERIC(12,2),
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL,
    CONSTRAINT fk_repairs_devices FOREIGN KEY (id_device) REFERENCES devices(id_device),
    CONSTRAINT fk_repairs_clients FOREIGN KEY (id_client) REFERENCES clients(id_client)
);

CREATE INDEX IF NOT EXISTS idx_devices_client_id ON devices(client_id);
CREATE INDEX IF NOT EXISTS idx_repairs_client_id ON repairs(id_client);
CREATE INDEX IF NOT EXISTS idx_repairs_device_id ON repairs(id_device);
CREATE INDEX IF NOT EXISTS idx_repairs_status ON repairs(status);
