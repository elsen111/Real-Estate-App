-- liquibase formatted sql

-- changeset Elshan:V2.0.16-insert-default-categories
-- comment: Insert default system categories for real estate properties.
INSERT INTO categories (id, name, slug, description)
VALUES
    (gen_random_uuid(), 'Apartment', 'apartment', 'Apartment properties'),
    (gen_random_uuid(), 'House', 'house', 'House properties'),
    (gen_random_uuid(),'Villa', 'villa', 'Villa properties'),
    (gen_random_uuid(), 'Office', 'office', 'Office properties'),
    (gen_random_uuid(), 'Land', 'land', 'Land properties'),
    (gen_random_uuid(), 'Commercial', 'commercial', 'Commercial real estate properties'),
    (gen_random_uuid(), 'Sea side', 'seaside', 'Seaside real estate properties');

--rollback DELETE FROM categories WHERE slug = 'seaside';