--liquibase formatted sql

--changeset Elshan:V1.0.22__create_payments_table
CREATE TABLE payments
(
    id                           UUID PRIMARY KEY,
    agency_id                    UUID           NOT NULL,
    plan_id                      UUID           NOT NULL,
    subscription_id              UUID,
    idempotency_key              VARCHAR(255)   NOT NULL,
    provider_checkout_session_id VARCHAR(255),
    provider_payment_intent_id   VARCHAR(255),
    amount                       NUMERIC(10, 2) NOT NULL,
    currency                     VARCHAR(3)     NOT NULL,
    status                       VARCHAR(30)    NOT NULL,
    checkout_url                 VARCHAR(1000),
    failure_reason               TEXT,
    paid_at                      TIMESTAMP,
    created_at                   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_agency_id
        FOREIGN KEY (agency_id)
            REFERENCES agencies (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_payments_plan_id
        FOREIGN KEY (plan_id)
            REFERENCES subscription_plans (id),

    CONSTRAINT fk_payments_subscription_id
        FOREIGN KEY (subscription_id)
            REFERENCES agency_subscriptions (id)
            ON DELETE SET NULL,

    CONSTRAINT uk_payments_agency_idempotency_key
        UNIQUE (agency_id, idempotency_key),

    CONSTRAINT uk_payments_checkout_session
        UNIQUE (provider_checkout_session_id),

    CONSTRAINT uk_payments_payment_intent
        UNIQUE (provider_payment_intent_id)
);

CREATE INDEX idx_payments_agency_id
    ON payments (agency_id);

CREATE INDEX idx_payments_plan_id
    ON payments (plan_id);

CREATE INDEX idx_payments_status
    ON payments (status);

CREATE INDEX idx_payments_agency_status
    ON payments (agency_id, status);

CREATE INDEX idx_payments_created_at
    ON payments (created_at);

--changeset Elshan:V1.0.22__create_payment_webhook_events_table
CREATE TABLE payment_webhook_events
(
    id                UUID PRIMARY KEY,
    provider_event_id VARCHAR(255) NOT NULL,
    event_type        VARCHAR(100) NOT NULL,
    processed_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_payment_webhook_provider_event
        UNIQUE (provider_event_id)
);

CREATE INDEX idx_payment_webhook_event_type
    ON payment_webhook_events (event_type);

CREATE INDEX idx_payment_webhook_processed_at
    ON payment_webhook_events (processed_at);