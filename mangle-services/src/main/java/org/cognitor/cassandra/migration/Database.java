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

import static java.lang.String.format;

import static org.cognitor.cassandra.migration.util.Ensure.notNull;
import static org.cognitor.cassandra.migration.util.Ensure.notNullOrEmpty;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.exceptions.DriverException;
import org.cognitor.cassandra.migration.cql.SimpleCQLLexer;
import org.cognitor.cassandra.migration.keyspace.Keyspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * This class represents the Cassandra database. It is used to retrieve the current version of the
 * database and to execute migrations.
 *
 * @author Patrick Kranz
 */
public class Database implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    /**
     * The name of the table that manages the migration scripts
     */
    private static final String SCHEMA_CF = "schema_migration";

    /**
     * Insert statement that logs a migration into the schema_migration table.
     */
    private static final String INSERT_MIGRATION =
            "insert into %s" + "(applied_successful, version, script_name, script, executed_at) values(?, ?, ?, ?, ?)";

    /**
     * Statement used to create the table that manages the migrations.
     */
    private static final String CREATE_MIGRATION_CF =
            "CREATE TABLE %s" + " (applied_successful boolean, version int, script_name varchar, script text,"
                    + " executed_at timestamp, PRIMARY KEY (applied_successful, version))";

    /**
     * The query that retrieves current schema version
     */
    private static final String VERSION_QUERY =
            "select version from %s where applied_successful = True " + "order by version desc limit 1";

    /**
     * Error message that is thrown if there is an error during the migration
     */
    private static final String MIGRATION_ERROR_MSG = "Error during migration of script %s while executing '%s'";

    private final String keyspaceName;
    private final Keyspace keyspace;
    private final Cluster cluster;
    private final Session session;
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.QUORUM;
    private final PreparedStatement logMigrationStatement;

    public Database(Cluster cluster, Keyspace keyspace) {
        this.cluster = notNull(cluster, "cluster");
        this.keyspace = notNull(keyspace, "keyspace");
        this.keyspaceName = keyspace.getKeyspaceName();
        this.consistencyLevel = notNull(consistencyLevel, "consistencyLevel");
        createKeyspaceIfRequired();
        session = cluster.connect(keyspaceName);
        ensureSchemaTable();
        this.logMigrationStatement = session.prepare(format(INSERT_MIGRATION, SCHEMA_CF));
    }

    private static List<String> expectedMigrationFailureMessages() {
        List<String> expectedMigrationFailureMessages = new ArrayList<>();
        expectedMigrationFailureMessages.add("a field of the same name already exists");
        expectedMigrationFailureMessages.add("it conflicts with an existing column");
        expectedMigrationFailureMessages.add("Unknown field ");
        return expectedMigrationFailureMessages;
    }

    private void createKeyspaceIfRequired() {
        if (keyspaceExists()) {
            return;
        }
        try (Session localSession = this.cluster.connect()) {
            localSession.execute(this.keyspace.getCqlStatement());
        } catch (DriverException exception) {
            throw new MigrationException(format("Unable to create keyspace %s.", keyspaceName), exception);
        }
    }

    private boolean keyspaceExists() {
        return cluster.getMetadata().getKeyspace(keyspace.getKeyspaceName()) != null;
    }

    /**
     * Creates a new instance of the database.
     *
     * @param cluster
     *            the cluster that is connected to a cassandra instance
     * @param keyspaceName
     *            the keyspace name that will be managed by this instance
     */
    public Database(Cluster cluster, String keyspaceName) {
        this.cluster = notNull(cluster, "cluster");
        this.keyspaceName = notNullOrEmpty(keyspaceName, "keyspaceName");
        this.keyspace = null;
        session = cluster.connect(keyspaceName);
        ensureSchemaTable();
        this.logMigrationStatement = session.prepare(format(INSERT_MIGRATION, SCHEMA_CF));
    }

    /**
     * Closes the underlying session object. The cluster will not be touched and will stay open.
     * Call this after all migrations are done. After calling this, this database instance can no
     * longer be used.
     */
    public void close() {
        this.session.close();
    }

    /**
     * Gets the current version of the database schema. This version is taken from the migration
     * table and represent the latest successful entry.
     *
     * @return the current schema version
     */
    public int getVersion() {
        ResultSet resultSet = session.execute(format(VERSION_QUERY, SCHEMA_CF));
        Row result = resultSet.one();
        if (result == null) {
            return 0;
        }
        return result.getInt(0);
    }

    /**
     * Returns the name of the keyspace managed by this instance.
     *
     * @return the name of the keyspace managed by this instance
     */
    public String getKeyspaceName() {
        return this.keyspaceName;
    }

    /**
     * Makes sure the schema migration table exists. If it is not available it will be created.
     */
    private void ensureSchemaTable() {
        if (schemaTablesIsNotExisting()) {
            createSchemaTable();
        }
    }

    private boolean schemaTablesIsNotExisting() {
        return cluster.getMetadata().getKeyspace(keyspaceName).getTable(SCHEMA_CF) == null;
    }

    private void createSchemaTable() {
        session.execute(format(CREATE_MIGRATION_CF, SCHEMA_CF));
    }

    /**
     * Executes the given migration to the database and logs the migration along with the output in
     * the migration table. In case of an error a {@link MigrationException} is thrown with the
     * cause of the error inside.
     *
     * @param migration
     *            the migration to be executed.
     * @throws MigrationException
     *             if the migration fails
     */
    public void execute(DbMigration migration) {
        notNull(migration, "migration");
        LOGGER.debug("About to execute migration {} to version {}", migration.getScriptName(), migration.getVersion());

        String lastStatement = null;
        SimpleCQLLexer lexer = new SimpleCQLLexer(migration.getMigrationScript());
        for (String statement : lexer.getCqlQueries()) {
            statement = statement.trim();
            lastStatement = statement;
            try {
                executeStatement(statement);
            } catch (Exception exception) {
                if (!StringUtils.isEmpty(expectedMigrationFailureMessages().stream()
                        .filter(message -> exception.getMessage().contains(message)).findAny().orElse(null))) {
                    continue;
                }
                logMigration(migration, false);
                String errorMessage = format(MIGRATION_ERROR_MSG, migration.getScriptName(), lastStatement);
                throw new MigrationException(errorMessage, exception, migration.getScriptName(), lastStatement);
            }
        }
        logMigration(migration, true);
        LOGGER.debug("Successfully applied migration {} to version {}", migration.getScriptName(),
                migration.getVersion());
    }

    private void executeStatement(String statement) {
        if (!statement.isEmpty()) {
            SimpleStatement simpleStatement = new SimpleStatement(statement);
            simpleStatement.setConsistencyLevel(this.consistencyLevel);
            session.execute(simpleStatement);
        }
    }

    /**
     * Inserts the result of the migration into the migration table
     *
     * @param migration
     *            the migration that was executed
     * @param wasSuccessful
     *            indicates if the migration was successful or not
     */
    private void logMigration(DbMigration migration, boolean wasSuccessful) {
        BoundStatement boundStatement = logMigrationStatement.bind(wasSuccessful, migration.getVersion(),
                migration.getScriptName(), migration.getMigrationScript(), new Date());
        session.execute(boundStatement);
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Set the consistency level that should be used for schema upgrades. Default is
     * <code>ConsistencyLevel.QUORUM</code>
     *
     * @param consistencyLevel
     *            the consistency level to be used. Must not be null.
     * @return the current database instance
     */
    public Database setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = notNull(consistencyLevel, "consistencyLevel");
        return this;
    }
}
