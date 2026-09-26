-- liquibase formatted sql

-- changeset backend_dev:create-property-views-table
CREATE TABLE property_views
(
    id          UUID NOT NULL,
    property_id UUID NOT NULL,
    viewer_id   UUID NULL,
    viewed_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_property_views PRIMARY KEY (id),
    CONSTRAINT fk_property_views_property_id FOREIGN KEY (property_id) REFERENCES properties (id),
    CONSTRAINT fk_property_views_viewer_id FOREIGN KEY (viewer_id) REFERENCES users (id)
);

CREATE INDEX idx_property_views_property_user_viewed_at ON property_views (property_id, viewer_id, viewed_at);
CREATE INDEX idx_property_views_property_id ON property_views (property_id);
