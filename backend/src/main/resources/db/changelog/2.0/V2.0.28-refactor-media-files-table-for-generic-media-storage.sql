--liquibase formatted sql

--changeset Elshan:V1.0.09-create-media-files-table
--comment Create media_files table

CREATE TABLE media_files
(
    id UUID PRIMARY KEY,

    storage_key VARCHAR(500) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,

    original_name VARCHAR(255) NOT NULL,

    mime_type VARCHAR(100) NOT NULL,
    extension VARCHAR(20),

    is_main BOOLEAN NOT NULL DEFAULT FALSE,

    file_size BIGINT NOT NULL,

    media_type VARCHAR(20) NOT NULL,

    width INT,
    height INT,

    duration_seconds INT,

    checksum VARCHAR(128),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_files_media_type
    ON media_files(media_type);