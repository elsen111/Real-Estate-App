INSERT INTO categories (id, name, slug, description)
VALUES
    (gen_random_uuid(), 'Apartment', 'apartment', 'Apartment properties'),
    (gen_random_uuid(), 'House', 'house', 'House properties'),
    (gen_random_uuid(),'Villa', 'villa', 'Villa properties'),
    (gen_random_uuid(), 'Office', 'office', 'Office properties'),
    (gen_random_uuid(), 'Land', 'land', 'Land properties'),
    (gen_random_uuid(), 'Commercial', 'commercial', 'Commercial real estate properties');