-- liquibase formatted sql

-- changeset Elshan:V2.0.39-add-new-column-to-categories-table
-- comment: Insert a new column - image to the categories table.

ALTER TABLE categories
    ADD COLUMN image VARCHAR DEFAULT '' NOT NULL;