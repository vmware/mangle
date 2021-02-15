--Changeset kumargautam:Alter credentialsspec table
ALTER TABLE credentialsspec add dbtype text;
ALTER TABLE credentialsspec add dbusername text;
ALTER TABLE credentialsspec add dbpassword text;
ALTER TABLE credentialsspec add dbport int;
ALTER TABLE credentialsspec add dbname text;
ALTER TABLE credentialsspec add dbsslenabled boolean;