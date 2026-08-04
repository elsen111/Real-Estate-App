-- liquibase formatted sql

-- changeset Elshan:V2.0.33
-- comment: Insert a new column to users table.


ALTER TABLE users
    ADD deleted BOOLEAN NOT NULL DEFAULT FALSE;