--liquibase formatted sql

--changeset Elshan:V1.0.14-create-refresh-tokens-table
--comment Create refresh_tokens table
CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL,
                                token_hash VARCHAR(500) NOT NULL,
                                expiry_date TIMESTAMP NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                revoked_at TIMESTAMP,
                                replaced_by_token_hash VARCHAR(255),
                                ip_address VARCHAR(100),
                                user_agent TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),

                                CONSTRAINT fk_refresh_tokens_user_id
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);