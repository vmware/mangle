--changeset dbhat : alter ResiliencyScoreTaskTrigger to add endtime and rename column task status
ALTER TYPE ResiliencyScoreTaskTrigger RENAME resiliencyScoreTaskStatus TO taskStatus AND statusmessage TO taskFailureReason;
ALTER TYPE ResiliencyScoreTaskTrigger ADD endTime text;