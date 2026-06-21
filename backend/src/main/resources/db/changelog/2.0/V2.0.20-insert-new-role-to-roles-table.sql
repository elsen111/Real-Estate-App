-- changeset Elshan:V2.0.20-insert-support-role
-- comment: Insert a new agency owner role.
INSERT INTO roles (id, name, description)
VALUES (gen_random_uuid(), 'AGENCY_OWNER', 'Head of the agency');