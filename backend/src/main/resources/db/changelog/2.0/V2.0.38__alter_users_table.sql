--liquibase formatted sql

--changeset Elshan:V2.0.38__alter_users_table
--comment Change the column default value to true

ALTER TABLE users
    ALTER COLUMN email_verified SET DEFAULT TRUE;
