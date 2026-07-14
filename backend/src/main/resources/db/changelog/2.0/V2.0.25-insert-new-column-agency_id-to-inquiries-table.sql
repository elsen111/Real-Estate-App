-- liquibase formatted sql

-- changeset Elshan:V2.0.25
-- comment: Insert a new column to inquiries table.


ALTER TABLE inquiries
    ADD agency_id UUID NOT NULL;

ALTER TABLE inquiries
    ADD CONSTRAINT fk_inquiries_agency_id
        FOREIGN KEY (agency_id)
            REFERENCES agencies (id);

ALTER TABLE inquiries
    ADD CONSTRAINT uq_inquiries_agency_id
        UNIQUE (agency_id);