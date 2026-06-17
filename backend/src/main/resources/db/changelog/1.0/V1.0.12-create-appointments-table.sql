--liquibase formatted sql

--changeset Elshan:V1.0.12-create-appointments-table
--comment Create appointments table
CREATE TABLE appointments (
                              id UUID PRIMARY KEY,
                              property_id UUID NOT NULL,
                              client_id UUID NOT NULL,
                              agent_id UUID,
                              appointment_type VARCHAR(30) NOT NULL DEFAULT 'PROPERTY_VIEWING',
                              preferred_date_time TIMESTAMP NOT NULL,
                              confirmed_date_time TIMESTAMP,
                              status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                              note TEXT,
                              response_note TEXT,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_appointments_property_id
                                  FOREIGN KEY (property_id)
                                      REFERENCES properties(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_appointments_client_id
                                  FOREIGN KEY (client_id)
                                      REFERENCES users(id),

                              CONSTRAINT fk_appointments_agent_id
                                  FOREIGN KEY (agent_id)
                                      REFERENCES users(id)
);

CREATE INDEX idx_appointments_property_id ON appointments(property_id);
CREATE INDEX idx_appointments_client_id ON appointments(client_id);
CREATE INDEX idx_appointments_agent_id ON appointments(agent_id);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_preferred_date_time ON appointments(preferred_date_time);