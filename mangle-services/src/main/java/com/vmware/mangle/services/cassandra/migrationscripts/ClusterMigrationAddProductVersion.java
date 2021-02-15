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

package com.vmware.mangle.services.cassandra.migrationscripts;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;
import io.smartcat.migration.exceptions.SchemaAgreementException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author chetanc
 */
@Log4j2
@Component
public class ClusterMigrationAddProductVersion extends SchemaMigration implements MangleDBMigration {

    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;

    @Value("${info.app.version}")
    private String currentProductVersion;

    private static final int SCRIPT_VERSION = 3;
    private static final String CLUSTER_TABLE_NAME = "cluster";
    private static final String PRODUCT_VERSION_COLUMN_NAME = "productversion";
    private static final String PREPARED_UPDATE =
            "UPDATE cluster SET productversion = ? WHERE id = ?";

    public ClusterMigrationAddProductVersion() {
        super(SCRIPT_VERSION);
    }

    @Override
    public String getDescription() {
        return "Upgrade Cluster Table";
    }

    @Override
    public void execute() throws MigrationException {
        addProductVersionColumn();
        initializeProductVersionColumn();
    }

    /**
     * add a productversion column to cluster table type int
     *
     * @throws SchemaAgreementException
     */
    public void addProductVersionColumn() throws SchemaAgreementException {
        SchemaStatement statement = SchemaBuilder.alterTable(CLUSTER_TABLE_NAME).addColumn(PRODUCT_VERSION_COLUMN_NAME)
                .type(DataType.varchar());

        executeWithSchemaAgreement(statement);
    }

    public void initializeProductVersionColumn() throws SchemaAgreementException {
        final Statement select = QueryBuilder.select().all().from(CLUSTER_TABLE_NAME);
        final ResultSet results = this.session.execute(select);
        PreparedStatement updateProductVersionStatement = session.prepare(PREPARED_UPDATE);
        for (final Row row : results) {
            String id = row.getString("id");
            BoundStatement boundStatement = updateProductVersionStatement.bind(currentProductVersion, id);
            executeWithSchemaAgreement(boundStatement);
        }
    }

    /**
     * check if upgrade script should proceed; true if there exists a column in UDT of type
     * vcenterconnectionproperties
     *
     * @return
     */
    public boolean proceedDBUpgrade() {
        return session.getCluster().getMetadata().getKeyspace(keyspace).getTable(CLUSTER_TABLE_NAME)
                .getColumn(PRODUCT_VERSION_COLUMN_NAME) == null;
    }

}