-- Preflight existing databases for duplicate pairs and nullable key columns before rollout.
-- Rollback: ALTER TABLE narrations DROP INDEX uk_narrations_location_language;
ALTER TABLE narrations
    ADD CONSTRAINT uk_narrations_location_language UNIQUE (location_id, language_id);
