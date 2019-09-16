--Changeset ranjans:Alter event eventdate schema to timestamp
DROP INDEX IF EXISTS event_eventdate_idx;
ALTER TABLE  event drop eventdate;
ALTER TABLE event add eventdate timestamp;
CREATE INDEX IF NOT EXISTS event_eventdate_idx ON event (eventdate);