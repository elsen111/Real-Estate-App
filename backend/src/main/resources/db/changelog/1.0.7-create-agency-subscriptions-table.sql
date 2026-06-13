CREATE TABLE agency_subscriptions (
                                      id UUID PRIMARY KEY,
                                      agency_id UUID NOT NULL,
                                      plan_id UUID NOT NULL,
                                      start_date DATE NOT NULL,
                                      end_date DATE NOT NULL,
                                      status VARCHAR(30) NOT NULL DEFAULT 'INACTIVE',
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_agency_subscriptions_agency_id
                                          FOREIGN KEY (agency_id)
                                              REFERENCES agencies(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_agency_subscriptions_plan_id
                                          FOREIGN KEY (plan_id)
                                              REFERENCES subscription_plans(id),

                                      CONSTRAINT chk_agency_subscriptions_dates
                                          CHECK (end_date >= start_date)
);

CREATE INDEX idx_agency_subscriptions_agency_id ON agency_subscriptions(agency_id);
CREATE INDEX idx_agency_subscriptions_plan_id ON agency_subscriptions(plan_id);
CREATE INDEX idx_agency_subscriptions_status ON agency_subscriptions(status);
CREATE INDEX idx_agency_subscriptions_end_date ON agency_subscriptions(end_date);