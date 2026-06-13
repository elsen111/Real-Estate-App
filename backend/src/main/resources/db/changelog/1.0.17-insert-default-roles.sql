INSERT INTO roles (id, name, description)
VALUES
    (gen_random_uuid(), 'SUPER_ADMIN', 'Platform administrator'),
    (gen_random_uuid(), 'AGENCY_ADMIN', 'Agency owner or manager'),
    (gen_random_uuid(), 'AGENT', 'Agency property agent'),
    (gen_random_uuid(), 'CLIENT', 'Buyer or renter');