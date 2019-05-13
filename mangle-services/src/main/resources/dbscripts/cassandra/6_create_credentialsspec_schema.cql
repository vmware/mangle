--Changeset kumargautam:Create credentialsspec schema
DROP TABLE IF EXISTS credentialsspec;

CREATE TABLE IF NOT EXISTS credentialsspec (name text,id text,password text,type text,username text,privatekey blob,kubeconfig blob,accesskeyid text,secretkey text,PRIMARY KEY (name));

CREATE INDEX IF NOT EXISTS credentialsspec_id_idx ON credentialsspec (id);
CREATE INDEX IF NOT EXISTS credentialsspec_type_idx ON credentialsspec (type);