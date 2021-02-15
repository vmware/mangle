--changeset chetanc : alter authentication table, add aduser, adpassword
ALTER TABLE authenticationprovider ADD aduser text;
ALTER TABLE authenticationprovider ADD aduserpassword text;