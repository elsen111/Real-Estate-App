--liquibase formatted sql

--changeset Elshan:V1.0.17-create-user-media-table
--comment Create user_media table

CREATE TABLE user_media
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    media_id UUID NOT NULL,

    purpose VARCHAR(30) NOT NULL,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_media_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_media_media
        FOREIGN KEY (media_id)
            REFERENCES media_files(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_user_media_user
    ON user_media(user_id);

CREATE INDEX idx_user_media_media
    ON user_media(media_id);

CREATE INDEX idx_user_media_purpose
    ON user_media(purpose);