ALTER TABLE users
    ADD COLUMN agency_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_agency
        FOREIGN KEY (agency_id)
            REFERENCES agencies(id);