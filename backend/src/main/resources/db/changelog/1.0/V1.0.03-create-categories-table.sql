--liquibase formatted sql

--changeset Elshan:V1.0.03-create-categories-table
--comment Create categories table
CREATE TABLE categories (
                            id UUID PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(100) NOT NULL,
                            description VARCHAR(255),
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT uk_categories_name UNIQUE (name),
                            CONSTRAINT uk_categories_slug UNIQUE (slug)
);