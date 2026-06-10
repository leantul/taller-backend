CREATE INDEX IF NOT EXISTS idx_repairs_status_return_date
    ON repairs(status, return_date_time DESC);

CREATE INDEX IF NOT EXISTS idx_repairs_receive_date
    ON repairs(receive_date_time DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_unread_event_date
    ON notifications(readed, event_date DESC);
