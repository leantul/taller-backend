CREATE TABLE IF NOT EXISTS app_users (
    id_user VARCHAR(36) PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    creation_date_time TIMESTAMP NOT NULL,
    modification_datetime TIMESTAMP NOT NULL
);
