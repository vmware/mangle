/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.cognitor.cassandra.migration;

import static org.cognitor.cassandra.migration.util.Ensure.notNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;

import com.vmware.mangle.utils.constants.Constants;

/**
 * The migration task is managing the database migrations. It checks which schema version is in the
 * database and retrieves the migrations that need to be applied from the repository. Those
 * migrations are than executed against the database.
 *
 * @author Patrick Kranz
 */
public class MigrationTask {
    private static final Logger LOGGER = getLogger(MigrationTask.class);

    private final Database database;
    private final MigrationRepository repository;

    /**
     * Creates a migration task that uses the given database and repository.
     *
     * @param database
     *            the database that should be migrated
     * @param repository
     *            the repository that contains the migration scripts
     */
    public MigrationTask(Database database, MigrationRepository repository) {
        this.database = notNull(database, "database");
        this.repository = notNull(repository, "repository");
    }

    /**
     * Start the actual migration. Take the version of the database, get all required migrations and
     * execute them or do nothing if the DB is already up to date.
     *
     * At the end the underlying database instance is closed.
     *
     * @throws MigrationException
     *             if a migration fails
     */
    public void migrate() {
        if (databaseIsUpToDate()) {
            LOGGER.info("Keyspace {} is already up to date at version {}", database.getKeyspaceName(),
                    database.getVersion());
            return;
        }

        List<DbMigration> migrations = repository.getMigrationsSinceVersion(database.getVersion());
        migrations.forEach(database::execute);
        LOGGER.info("Migrated keyspace {} to version {}", database.getKeyspaceName(), database.getVersion());
        database.close();
        setSchemaMigratedConstant();
    }

    private static void setSchemaMigratedConstant() {
        Constants.setSchemaMigrated(true);
    }

    private boolean databaseIsUpToDate() {
        return database.getVersion() >= repository.getLatestVersion();
    }
}
