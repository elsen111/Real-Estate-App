INSERT INTO subscription_plans (
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
        'Enterprise',
        'Advanced plan for large agencies',
        149.99,
        30,
        500,
        50,
        TRUE,
        TRUE
    );