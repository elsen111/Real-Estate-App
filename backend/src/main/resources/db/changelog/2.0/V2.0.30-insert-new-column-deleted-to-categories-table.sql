-- liquibase formatted sql

-- changeset Elshan:V2.0.30
-- comment: Insert a new column to categories table.


ALTER TABLE categories
    ADD deleted BOOLEAN NOT NULL DEFAULT FALSE;