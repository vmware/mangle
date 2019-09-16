--Changeset kumargautam:Default roles
INSERT INTO role JSON '{"id":"5bb71fc82ff9a91aa88ebee3_ROLE_READONLY","name":"ROLE_READONLY","type":"MANGLE_DEFAULT","privilegeNames":["READONLY"]}';
INSERT INTO role JSON '{"id":"5bb71fc82ff9a91aa88ebee2_ROLE_USER","name":"ROLE_USER","type":"MANGLE_DEFAULT","privilegeNames":["ADMIN_READ","USER_READ_WRITE"]}';
INSERT INTO role JSON '{"id":"5bb71fc82ff9a91aa88ebee1_ROLE_ADMIN","name":"ROLE_ADMIN","type":"MANGLE_DEFAULT","privilegeNames":["ADMIN_READ_WRITE","USER_READ_WRITE"]}';