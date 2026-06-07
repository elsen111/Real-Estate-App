CREATE TABLE agency_members (
                                id BIGSERIAL PRIMARY KEY,
                                agency_id BIGINT NOT NULL,
                                user_id BIGINT NOT NULL,
                                position VARCHAR(100),
                                member_type VARCHAR(30) NOT NULL,
                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_agency_members_agency_id
                                    FOREIGN KEY (agency_id)
                                        REFERENCES agencies(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_agency_members_user_id
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT uk_agency_members_agency_user UNIQUE (agency_id, user_id)
);

CREATE INDEX idx_agency_members_agency_id ON agency_members(agency_id);
CREATE INDEX idx_agency_members_user_id ON agency_members(user_id);
CREATE INDEX idx_agency_members_member_type ON agency_members(member_type);