-- changeset Elshan:V2.0.26
-- comment: Drop the uniqueness constraint on agency_id to allow multiple inquiries per agency.

ALTER TABLE inquiries
DROP CONSTRAINT uq_inquiries_agency_id;
