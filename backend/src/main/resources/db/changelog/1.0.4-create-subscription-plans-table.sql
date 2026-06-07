CREATE TABLE subscription_plans (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL,
                                    description TEXT,
                                    price DECIMAL(10,2) NOT NULL,
                                    duration_days INT NOT NULL,
                                    max_listings INT NOT NULL,
                                    max_agents INT NOT NULL,
                                    featured_listings_allowed BOOLEAN NOT NULL DEFAULT FALSE,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT uk_subscription_plans_name UNIQUE (name),
                                    CONSTRAINT chk_subscription_plans_price CHECK (price >= 0),
                                    CONSTRAINT chk_subscription_plans_duration_days CHECK (duration_days > 0),
                                    CONSTRAINT chk_subscription_plans_max_listings CHECK (max_listings >= 0),
                                    CONSTRAINT chk_subscription_plans_max_agents CHECK (max_agents >= 0)
);

CREATE INDEX idx_subscription_plans_active ON subscription_plans(active);