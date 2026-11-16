--liquibase formatted sql

--changeset Elshan:add-users-audit-fields

ALTER TABLE users
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_deleted_by
        FOREIGN KEY (deleted_by)
            REFERENCES users (id);