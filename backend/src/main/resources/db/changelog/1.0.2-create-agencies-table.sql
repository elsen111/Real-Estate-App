CREATE TABLE agencies (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          description TEXT,
                          phone_number VARCHAR(30) NOT NULL,
                          email VARCHAR(150) NOT NULL,
                          website VARCHAR(255),
                          logo_url VARCHAR(500),
                          city VARCHAR(100) NOT NULL,
                          address VARCHAR(255) NOT NULL,
                          status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_agencies_city ON agencies(city);
CREATE INDEX idx_agencies_status ON agencies(status);
CREATE INDEX idx_agencies_name ON agencies(name);