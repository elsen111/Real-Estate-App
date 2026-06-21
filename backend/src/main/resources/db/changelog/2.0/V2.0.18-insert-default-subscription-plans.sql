-- liquibase formatted sql

-- changeset Elshan:V2.0.18-insert-default-subscription-plans
-- comment: Insert default system subscription plans.
INSERT INTO subscription_plans (
    id,
    name,
    description,
    price,
    duration_days,
    max_listings,
    max_agents,
    featured_listings_allowed,
    active
)
VALUES
    (
        gen_random_uuid(),
        'Starter',
        'Basic plan for small agencies',
        29.99,
        30,
        20,
        3,
        FALSE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'Professional',
        'Standard plan for growing agencies',
        79.99,
        30,
        100,
        10,
        TRUE,
        TRUE
    ),
    (
        gen_random_uuid(),
        'Enterprise',
        'Advanced plan for large agencies',
        149.99,
        30,
        500,
        50,
        TRUE,
        TRUE
    );