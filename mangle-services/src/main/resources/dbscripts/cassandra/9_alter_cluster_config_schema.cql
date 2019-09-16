--Changeset chetanc:Alter cluster table
ALTER TABLE cluster add master varchar;
ALTER TABLE cluster add quorum int;
ALTER TABLE cluster add deploymentMode varchar;