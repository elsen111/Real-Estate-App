-- liquibase formatted sql

-- changeset Elshan:V2.0.31
-- comment: Insert a new column to reviews table.


ALTER TABLE reviews
    ADD target VARCHAR NOT NULL;