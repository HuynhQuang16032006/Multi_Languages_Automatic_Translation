-- Existing locations remain visible; no history or location data is removed.
ALTER TABLE locations ADD COLUMN visible BOOLEAN NOT NULL DEFAULT TRUE;

-- Serialize hierarchy and visibility changes across application instances.
CREATE TABLE location_hierarchy_lock (
    id INTEGER NOT NULL PRIMARY KEY
);
INSERT INTO location_hierarchy_lock (id) VALUES (1);
