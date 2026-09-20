-- liquibase formatted sql

-- changeset Elshan:V2.0.31
-- comment: Insert a new column to reviews table.

ALTER TABLE properties
    ADD COLUMN average_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0;