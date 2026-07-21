UPDATE repairs
SET labor_amount = 0
WHERE labor_amount IS NULL;

ALTER TABLE repairs
    ALTER COLUMN labor_amount SET DEFAULT 0;

ALTER TABLE repairs
    ALTER COLUMN labor_amount SET NOT NULL;
