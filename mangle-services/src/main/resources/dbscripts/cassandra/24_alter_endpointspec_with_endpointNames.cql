--Changeset bkaranam:Alter endpointspec table with endpointNames column
ALTER TABLE endpointspec add endpointNames List<text>;
ALTER TABLE endpointspec add endpointGroupType text;