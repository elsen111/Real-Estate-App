-- liquibase formatted sql

-- changeset Elshan:V2.0.23
-- comment: Insert a new column to subscription plans table.


ALTER TABLE subscription_plans
    ADD deleted BOOLEAN NOT NULL DEFAULT FALSE;