CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       description VARCHAR(255),

                       CONSTRAINT uk_roles_name UNIQUE (name)
);