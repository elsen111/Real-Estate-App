--liquibase formatted sql

--changeset Elshan:V1.0.13-create-reviews-table
--comment Create reviews table
CREATE TABLE reviews (
                         id UUID PRIMARY KEY,
                         reviewer_id UUID NOT NULL,
                         agency_id UUID,
                         property_id UUID,
                         rating INT NOT NULL,
                         comment TEXT,
                         status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_reviews_reviewer_id
                             FOREIGN KEY (reviewer_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_reviews_agency_id
                             FOREIGN KEY (agency_id)
                                 REFERENCES agencies(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_reviews_property_id
                             FOREIGN KEY (property_id)
                                 REFERENCES properties(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT chk_reviews_rating
                             CHECK (rating BETWEEN 1 AND 5),

                         CONSTRAINT chk_reviews_target
                             CHECK (
                                 agency_id IS NOT NULL
                                     OR property_id IS NOT NULL
                                 )
);

CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_reviews_agency_id ON reviews(agency_id);
CREATE INDEX idx_reviews_property_id ON reviews(property_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_status ON reviews(status);