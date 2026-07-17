--liquibase formatted sql

--changeset Elshan:V1.0.16-create-agency-media-table
--comment Create agency_media table

CREATE TABLE agency_media
(
    id UUID PRIMARY KEY,

    agency_id UUID NOT NULL,
    media_id UUID NOT NULL,

    purpose VARCHAR(30) NOT NULL,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_agency_media_agency
        FOREIGN KEY (agency_id)
            REFERENCES agencies(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_agency_media_media
        FOREIGN KEY (media_id)
            REFERENCES media_files(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_agency_media_agency
    ON agency_media(agency_id);

CREATE INDEX idx_agency_media_media
    ON agency_media(media_id);

CREATE INDEX idx_agency_media_purpose
    ON agency_media(purpose);