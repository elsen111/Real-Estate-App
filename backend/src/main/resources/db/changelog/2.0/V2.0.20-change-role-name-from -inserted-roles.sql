-- liquibase formatted sql

-- changeset Elshan:V2.0.20-rename-landlord-role
-- comment: Rename role from Landlord to LANDLORD.

UPDATE roles
SET name = 'LANDLORD'
WHERE name = 'Landlord';