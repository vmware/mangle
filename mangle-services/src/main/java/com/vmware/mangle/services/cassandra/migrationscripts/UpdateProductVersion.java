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
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ranjans
 */
@Log4j2
@Component
public class UpdateProductVersion extends SchemaMigration implements MangleDBMigration {

    public UpdateProductVersion(@Value(value = "${app.release.cluster.version}") String scriptVersion) {
        super(Integer.parseInt(scriptVersion));
    }

    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;

    @Value("${info.app.version}")
    private String currentProductVersion;

    private static final String CLUSTER_TABLE_NAME = "cluster";
    private static final String PRODUCT_VERSION_COLUMN_NAME = "productversion";
    private static final String PREPARED_UPDATE = "UPDATE cluster SET productversion = ? WHERE id = ?";

    @Override
    public boolean proceedDBUpgrade() {
        return session.getCluster().getMetadata().getKeyspace(keyspace).getTable(CLUSTER_TABLE_NAME)
                .getColumn(PRODUCT_VERSION_COLUMN_NAME) != null;
    }

    @Override
    public String getDescription() {
        return "Updating product version...";
    }

    @Override
    public void execute() throws MigrationException {
        log.info("Updating product version to " + currentProductVersion);
        final Statement select = QueryBuilder.select().all().from(CLUSTER_TABLE_NAME);
        final ResultSet results = this.session.execute(select);
        PreparedStatement updateProductVersionStatement = session.prepare(PREPARED_UPDATE);
        for (final Row row : results) {
            String id = row.getString("id");
            BoundStatement boundStatement = updateProductVersionStatement.bind(currentProductVersion, id);
            executeWithSchemaAgreement(boundStatement);
        }
    }

}
