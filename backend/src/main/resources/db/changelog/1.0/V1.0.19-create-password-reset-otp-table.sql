--liquibase formatted sql

--changeset Elshan:V1.0.19-create-password-reset-otp-table
--comment Create password-reset-otp table

CREATE TABLE password_reset_otp
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    otp VARCHAR(6) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    used BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user
    ON password_reset_otp(user_id);

CREATE INDEX idx_password_reset_otp
    ON password_reset_otp(otp);