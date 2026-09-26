CREATE INDEX idx_agency_subscriptions_status_end_date
    ON agency_subscriptions (status, end_date);