--liquibase formatted sql

--changeset Elshan:V1.0.15-create-password-reset-tokens-table
--comment Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
                                       id UUID PRIMARY KEY,
                                       user_id UUID NOT NULL,
                                       token VARCHAR(500) NOT NULL,
                                       expiry_date TIMESTAMP NOT NULL,
                                       used BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT uk_password_reset_tokens_token UNIQUE (token),

                                       CONSTRAINT fk_password_reset_tokens_user_id
                                           FOREIGN KEY (user_id)
                                               REFERENCES users(id)
                                               ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_used ON password_reset_tokens(used);
CREATE INDEX idx_password_reset_tokens_expiry_date ON password_reset_tokens(expiry_date);