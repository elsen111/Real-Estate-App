--liquibase formatted sql

--changeset Elshan:V1.0.18-create-property-media-table
--comment Create property_media table

CREATE TABLE property_media
(
    id UUID PRIMARY KEY,

    property_id UUID NOT NULL,
    media_id UUID NOT NULL,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    sort_order INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_property_media_property
        FOREIGN KEY (property_id)
            REFERENCES properties(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_property_media_media
        FOREIGN KEY (media_id)
            REFERENCES media_files(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_property_media_property
    ON property_media(property_id);

CREATE INDEX idx_property_media_media
    ON property_media(media_id);

CREATE INDEX idx_property_media_primary
    ON property_media(is_primary);