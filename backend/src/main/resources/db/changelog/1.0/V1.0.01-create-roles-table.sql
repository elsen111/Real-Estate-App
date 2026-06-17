--liquibase formatted sql

--changeset Elshan:V1.0.01-create-roles-table
--comment Create roles table
CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       description VARCHAR(255),

                       CONSTRAINT uk_roles_name UNIQUE (name)
);
--rollback DROP TABLE IF EXISTS roles CASCADE;