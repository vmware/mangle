--changeset dbhat : alter resiliencyscore_task table, add taskdata, taskType
ALTER TABLE resiliencyscore_task ADD taskdata text;
ALTER TABLE resiliencyscore_task ADD taskType text;
ALTER TABLE resiliencyscore_task ADD taskName text;
ALTER TABLE resiliencyscore_task ADD taskDescription text;