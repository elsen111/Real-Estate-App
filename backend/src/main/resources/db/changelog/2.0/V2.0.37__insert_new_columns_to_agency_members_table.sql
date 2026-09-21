--liquibase formatted sql

--changeset Elshan:add-agency-member-audit-fields

ALTER TABLE agency_members
    ADD COLUMN role VARCHAR(30),
    ADD COLUMN added_by UUID,
    ADD COLUMN removed_by UUID;

ALTER TABLE agency_members
    ADD CONSTRAINT fk_agency_members_added_by
        FOREIGN KEY (added_by)
            REFERENCES users (id);

ALTER TABLE agency_members
    ADD CONSTRAINT fk_agency_members_removed_by
        FOREIGN KEY (removed_by)
            REFERENCES users (id);