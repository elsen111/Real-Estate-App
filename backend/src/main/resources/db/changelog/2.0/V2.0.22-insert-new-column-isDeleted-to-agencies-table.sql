-- liquibase formatted sql

-- changeset Elshan:V2.0.22-rename-landlord-role
-- comment: Insert a new column to agencies table.


ALTER TABLE agencies
ADD is_deleted BOOLEAN NOT NULL DEFAULT TRUE;