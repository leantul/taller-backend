-- Apply to existing PostgreSQL databases before deploying the updated application.
-- New databases get a TEXT column from the Notification entity mapping.
ALTER TABLE notifications ALTER COLUMN message TYPE TEXT;
