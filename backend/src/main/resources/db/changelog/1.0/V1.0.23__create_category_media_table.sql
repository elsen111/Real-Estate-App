--liquibase formatted sql

--changeset Elshan:V1.0.23-create-category-media-table
--comment Create category_media table

CREATE TABLE category_media
(
    id          UUID PRIMARY KEY,
    category_id UUID      NOT NULL,
    media_id    UUID      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_category_media_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_category_media_media
        FOREIGN KEY (media_id)
            REFERENCES media_files (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_category_media_category
    ON category_media (category_id);

CREATE INDEX idx_category_media_media
    ON category_media (media_id);