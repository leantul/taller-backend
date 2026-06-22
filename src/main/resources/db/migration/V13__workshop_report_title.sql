ALTER TABLE workshop_settings
    ADD COLUMN IF NOT EXISTS report_title VARCHAR(200);

UPDATE workshop_settings
SET report_title = 'REPORTE DE REPARACIÓN'
WHERE report_title IS NULL OR trim(report_title) = '';

ALTER TABLE workshop_settings
    ALTER COLUMN report_title SET NOT NULL;
