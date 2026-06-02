ALTER TABLE devices ADD COLUMN IF NOT EXISTS technical_details TEXT;

UPDATE devices
SET technical_details = CONCAT_WS(
    E'\n',
    CASE
        WHEN NULLIF(TRIM(accessories), '') IS NOT NULL THEN CONCAT('Accesorios: ', accessories)
        ELSE NULL
    END,
    CASE
        WHEN NULLIF(TRIM(aesthetic_condition), '') IS NOT NULL THEN CONCAT('Estado estético: ', aesthetic_condition)
        ELSE NULL
    END
)
WHERE NULLIF(TRIM(COALESCE(technical_details, '')), '') IS NULL
  AND (
      NULLIF(TRIM(COALESCE(accessories, '')), '') IS NOT NULL
      OR NULLIF(TRIM(COALESCE(aesthetic_condition, '')), '') IS NOT NULL
  );
