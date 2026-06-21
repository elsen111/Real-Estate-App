--liquibase formatted sql

--changeset Elshan:V1.0.11-create-inquiries-table
--comment Create inquiries table
CREATE TABLE inquiries (
                           id UUID PRIMARY KEY,
                           property_id UUID NOT NULL,
                           client_id UUID NOT NULL,
                           assigned_agent_id UUID,
                           message TEXT NOT NULL,
                           preferred_contact_method VARCHAR(30) NOT NULL,
                           status VARCHAR(30) NOT NULL DEFAULT 'NEW',
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_inquiries_property_id
                               FOREIGN KEY (property_id)
                                   REFERENCES properties(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_inquiries_client_id
                               FOREIGN KEY (client_id)
                                   REFERENCES users(id),

                           CONSTRAINT fk_inquiries_assigned_agent_id
                               FOREIGN KEY (assigned_agent_id)
                                   REFERENCES users(id)
);

CREATE INDEX idx_inquiries_property_id ON inquiries(property_id);
CREATE INDEX idx_inquiries_client_id ON inquiries(client_id);
CREATE INDEX idx_inquiries_assigned_agent_id ON inquiries(assigned_agent_id);
CREATE INDEX idx_inquiries_status ON inquiries(status);
CREATE INDEX idx_inquiries_created_at ON inquiries(created_at);