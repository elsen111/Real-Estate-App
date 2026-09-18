-- liquibase formatted sql

-- changeset Elshan:V2.0.34
-- comment: Create subscription notifications table for scheduled subscription emails.

CREATE TABLE subscription_notifications (
                                            id UUID PRIMARY KEY,
                                            subscription_id UUID NOT NULL,
                                            type VARCHAR(50) NOT NULL,
                                            status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                            attempts INTEGER NOT NULL DEFAULT 0,
                                            locked_at TIMESTAMP,
                                            sent_at TIMESTAMP,
                                            last_error TEXT,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                            CONSTRAINT fk_subscription_notifications_subscription_id
                                                FOREIGN KEY (subscription_id)
                                                    REFERENCES agency_subscriptions(id)
                                                    ON DELETE CASCADE,

                                            CONSTRAINT uk_subscription_notifications_subscription_type
                                                UNIQUE (subscription_id, type)
);

CREATE INDEX idx_subscription_notifications_status_created_at
    ON subscription_notifications(status, created_at);

CREATE INDEX idx_subscription_notifications_locked_at
    ON subscription_notifications(locked_at);