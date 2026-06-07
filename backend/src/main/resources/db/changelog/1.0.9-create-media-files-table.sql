CREATE TABLE media_files (
                             id BIGSERIAL PRIMARY KEY,

                             property_id BIGINT,
                             agency_id BIGINT,

                             file_url VARCHAR(500) NOT NULL,
                             file_name VARCHAR(255),
                             file_type VARCHAR(50),
                             file_size BIGINT,

                             media_purpose VARCHAR(50) NOT NULL,

                             is_main BOOLEAN NOT NULL DEFAULT FALSE,
                             sort_order INT DEFAULT 0,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_media_files_property
                                 FOREIGN KEY (property_id)
                                     REFERENCES properties(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_media_files_agency
                                 FOREIGN KEY (agency_id)
                                     REFERENCES agencies(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT chk_media_files_owner
                                 CHECK (
                                     (property_id IS NOT NULL AND agency_id IS NULL)
                                         OR
                                     (property_id IS NULL AND agency_id IS NOT NULL)
                                     )
);

CREATE INDEX idx_media_files_property_id ON media_files(property_id);
CREATE INDEX idx_media_files_agency_id ON media_files(agency_id);
CREATE INDEX idx_media_files_purpose ON media_files(media_purpose);