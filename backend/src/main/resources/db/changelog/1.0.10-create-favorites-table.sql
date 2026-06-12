CREATE TABLE favorites (
                           id UUID PRIMARY KEY,
                           user_id UUID NOT NULL,
                           property_id UUID NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_favorites_user_id
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_favorites_property_id
                               FOREIGN KEY (property_id)
                                   REFERENCES properties(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT uk_favorites_user_property UNIQUE (user_id, property_id)
);

CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_property_id ON favorites(property_id);