--liquibase formatted sql

--changeset Elshan:V1.0.08-create-properties-table
--comment Create properties table
CREATE TABLE properties (
                            id UUID PRIMARY KEY,
                            agency_id UUID NOT NULL,
                            category_id UUID NOT NULL,
                            assigned_agent_id UUID,
                            title VARCHAR(200) NOT NULL,
                            description TEXT NOT NULL,
                            price DECIMAL(15,2) NOT NULL,
                            city VARCHAR(100) NOT NULL,
                            district VARCHAR(100),
                            address VARCHAR(255) NOT NULL,
                            listing_type VARCHAR(30) NOT NULL,
                            area DECIMAL(10,2) NOT NULL,
                            rooms INT,
                            bathrooms INT,
                            floor INT,
                            total_floors INT,
                            latitude DECIMAL(10,7) NOT NULL,
                            longitude DECIMAL(10,7) NOT NULL,
                            status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                            featured BOOLEAN NOT NULL DEFAULT FALSE,
                            view_count BIGINT NOT NULL DEFAULT 0,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_properties_agency_id
                                FOREIGN KEY (agency_id)
                                    REFERENCES agencies(id),

                            CONSTRAINT fk_properties_category_id
                                FOREIGN KEY (category_id)
                                    REFERENCES categories(id),

                            CONSTRAINT fk_properties_assigned_agent_id
                                FOREIGN KEY (assigned_agent_id)
                                    REFERENCES users(id)
);

CREATE INDEX idx_properties_agency_id ON properties(agency_id);
CREATE INDEX idx_properties_category_id ON properties(category_id);
CREATE INDEX idx_properties_assigned_agent_id ON properties(assigned_agent_id);
CREATE INDEX idx_properties_city ON properties(city);
CREATE INDEX idx_properties_district ON properties(district);
CREATE INDEX idx_properties_listing_type ON properties(listing_type);
CREATE INDEX idx_properties_status ON properties(status);
CREATE INDEX idx_properties_price ON properties(price);
CREATE INDEX idx_properties_location ON properties(latitude, longitude);
CREATE INDEX idx_properties_featured ON properties(featured);
CREATE INDEX idx_properties_created_at ON properties(created_at);