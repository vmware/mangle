--Changeset dbhat:Alter metricprovider table to add Dynatrace Connection Properties column
ALTER TABLE metricprovider add dynatraceConnectionProperties frozen<dynatraceConnectionProperties>;