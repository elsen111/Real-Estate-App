
--liquibase formatted sql

--changeset Elshan:V2.0.40-update-password-reset-security
--comment Secure password reset OTP and token storage

-- 1. Update password_reset_otp

ALTER TABLE password_reset_otp
    RENAME COLUMN otp TO otp_hash;

ALTER TABLE password_reset_otp
ALTER COLUMN otp_hash TYPE VARCHAR(64);

ALTER TABLE password_reset_otp
    ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0;

DROP INDEX IF EXISTS idx_password_reset_otp;

-- 2. Update password_reset_tokens

ALTER TABLE password_reset_tokens
    RENAME COLUMN token TO token_hash;

ALTER TABLE password_reset_tokens
ALTER COLUMN token_hash TYPE VARCHAR(64);

ALTER TABLE password_reset_tokens
    RENAME COLUMN expiry_date TO expires_at;

ALTER TABLE password_reset_tokens
DROP COLUMN used;

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT uk_password_reset_tokens_user_id
        UNIQUE (user_id);

-- 3. Create indexes

CREATE INDEX idx_password_reset_otp_user_created
    ON password_reset_otp (user_id, created_at DESC);

CREATE INDEX idx_password_reset_token_hash
    ON password_reset_tokens (token_hash);
