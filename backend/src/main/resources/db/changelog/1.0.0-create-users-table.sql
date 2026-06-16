CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(30),
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled);