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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.CommonUtils;

/**
 * @author ranjans
 */
@Log4j2
@Component
public class TaskLastUpdatedFieldInitialize extends SchemaMigration implements MangleDBMigration {

    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;
    private static final String CREATE_LAST_UPDATED = "ALTER TABLE task add lastupdated bigint";
    private static final String UPDATE_TASK_STATEMENT = "UPDATE task SET lastupdated = ? WHERE id = ?";
    private static final String TASK_TABLE = "task";
    private static final String LAST_UPDATED = "lastupdated";
    private static final String TRIGGERS = "triggers";
    private static final String START_TIME = "starttime";
    private static final String END_TIME = "endtime";
    private static final String TASK_ID = "id";
    private static final int SCRIPT_VERSION = 2;

    public TaskLastUpdatedFieldInitialize() {
        super(SCRIPT_VERSION);
    }

    @Override
    public boolean proceedDBUpgrade() {
        return !isLastUpdatedFieldExist(session.getCluster().getMetadata().getKeyspace(keyspace).getTable(TASK_TABLE));
    }

    @Override
    public String getDescription() {
        return "Update task's lastupdated field.";
    }

    @Override
    public void execute() throws MigrationException {
        log.info("Creating lastupdated field");
        session.execute(CREATE_LAST_UPDATED);
        CommonUtils.delayInSeconds(5);
        for (int i = 0; i < 2; i++) {
            executeStatement();
        }
    }

    private void executeStatement() throws MigrationException {
        PreparedStatement preparedStatement = session.prepare(UPDATE_TASK_STATEMENT);
        BoundStatement boundStatement;
        final Statement statement = QueryBuilder.select().all().from(TASK_TABLE);
        final ResultSet resultSet = this.session.execute(statement);
        for (final Row row : resultSet) {
            if (row.get(LAST_UPDATED, Long.class) == null) {
                Long lastUpdatedFieldVal = System.currentTimeMillis();
                List<UDTValue> udtDataList = row.getList(TRIGGERS, UDTValue.class);
                if (!udtDataList.isEmpty()) {
                    if (udtDataList.get(0).getString(END_TIME) != null) {
                        lastUpdatedFieldVal = getDateTimeMilli(udtDataList.get(0).getString(END_TIME));
                    } else if (udtDataList.get(0).getString(START_TIME) != null) {
                        lastUpdatedFieldVal = getDateTimeMilli(udtDataList.get(0).getString(START_TIME));
                    } else {
                        lastUpdatedFieldVal = System.currentTimeMillis();
                    }
                }
                log.info("Updating lastupdated=" + lastUpdatedFieldVal + " for task id=" + row.getString(TASK_ID));
                boundStatement = preparedStatement.bind(lastUpdatedFieldVal, row.getString(TASK_ID));
                executeWithSchemaAgreement(boundStatement);
            }
        }
    }

    private boolean isLastUpdatedFieldExist(TableMetadata tableMetadata) {
        List<ColumnMetadata> columnMetadataList = tableMetadata.getColumns();
        for (ColumnMetadata columnMetadata : columnMetadataList) {
            if (columnMetadata.getName().matches(LAST_UPDATED)) {
                return true;
            }
        }
        return false;
    }

    private Long getDateTimeMilli(String dateTime) throws MigrationException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
        Date date;
        try {
            date = sdf.parse(dateTime);
        } catch (ParseException e) {
            throw new MigrationException("Error during parsing date", e);
        }
        return date.getTime();
    }

}
