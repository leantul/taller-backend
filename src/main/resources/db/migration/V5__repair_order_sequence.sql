CREATE SEQUENCE IF NOT EXISTS repair_order_seq START WITH 1 INCREMENT BY 1;

SELECT setval('repair_order_seq', COALESCE((SELECT MAX(CAST(order_number AS INTEGER)) FROM repairs WHERE order_number ~ '^[0-9]+$'), 0));

CREATE UNIQUE INDEX IF NOT EXISTS ux_repairs_order_number ON repairs(order_number);
