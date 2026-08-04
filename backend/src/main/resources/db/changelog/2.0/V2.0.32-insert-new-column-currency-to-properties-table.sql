-- liquibase formatted sql

-- changeset Elshan:V2.0.32
-- comment: Insert a new column to properties table.


ALTER TABLE properties
    ADD currency VARCHAR(3) NOT NULL DEFAULT 'USD';