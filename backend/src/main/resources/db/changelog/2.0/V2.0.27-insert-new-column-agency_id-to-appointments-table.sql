-- liquibase formatted sql

-- changeset Elshan:V2.0.27
-- comment: Insert a new column to appointments table.


ALTER TABLE appointments
    ADD agency_id UUID NOT NULL;

ALTER TABLE inquiries
    ADD CONSTRAINT fk_appointments_agency_id
        FOREIGN KEY (agency_id)
            REFERENCES agencies (id);