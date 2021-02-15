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

import static com.datastax.driver.core.DataType.text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.CreateType;
import com.datastax.driver.core.schemabuilder.Drop;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;
import io.smartcat.migration.exceptions.SchemaAgreementException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.endpoint.VCenterConnectionProperties;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * @author chetanc
 */
@Log4j2
@Component
public class VCenterConnectionPropertiesUpgrade extends SchemaMigration implements MangleDBMigration {

    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;

    private static final int SCRIPT_VERSION = 1;

    private static final String PREPARED_INSERT_STRING_VCA_ADAPTER =
            "INSERT INTO vcenteradapter (name, adapterUrl, username, password) VALUES (?, ?, ?, ?);";
    private static final String PREPARED_UPDATE =
            "UPDATE endpointspec SET vcenterconnectionproperties = ? WHERE name = ?";
    private Map<String, VCenterConnectionProperties> endpointToEndpointSpec = new HashMap<>();

    private static final String VCENTER_CONNECTION_PROERPTIES_COL = "vcenterconnectionproperties";
    private static final String VCENTER_ADAPTER_PROERPTIES_UDT_COL = "vcenteradapterproperties";
    private static final String VCENTER_ADAPTER_DETAILS_NAME_UDT_COL = "vcenteradapterdetailsname";
    private static final String ENDPOINT_TABLE = "endpointspec";
    private static final String ENDPOINT_TYPE_COLUMN = "endpointtype";
    private static final String HOSTNAME_COL_NAME = "hostname";
    private PreparedStatement vcAdapterAddRowPreparedStatement;

    public VCenterConnectionPropertiesUpgrade() {
        super(SCRIPT_VERSION);
    }

    @Override
    public String getDescription() {
        return "Upgrade Vcenter Properties Table";
    }

    @Override
    public void execute() throws MigrationException {
        createDataForMigration();
        removeVCenterConnectionPropertiesUDTFromEndpoint();
        dropVCenterConnectionPropertiesUDTType();
        createNewVCenterConnectionPropertiesUDT();
        addNewVCenterConnectionPropertiesUDTColumnToEndpoint();
        if (!CollectionUtils.isEmpty(endpointToEndpointSpec)) {
            addNewUDTValuesToEndpoint();
        }
    }

    /**
     * Find the rows which are related to endpoint of type VCENTER, and creates an entry for each row of
     * endpoint in vcenteradapter table
     *
     * @throws SchemaAgreementException
     */
    private void createDataForMigration() throws SchemaAgreementException {
        vcAdapterAddRowPreparedStatement = session.prepare(PREPARED_INSERT_STRING_VCA_ADAPTER);
        final Statement select = QueryBuilder.select().all().from(ENDPOINT_TABLE);
        final ResultSet results = this.session.execute(select);
        for (final Row row : results) {
            if (row.getString(ENDPOINT_TYPE_COLUMN).equals(EndpointType.VCENTER.name()) && isVCColumnExists(row)) {
                final String endpointName = row.getString("name");
                final String hostname =
                        ((UDTValue) row.getObject(VCENTER_CONNECTION_PROERPTIES_COL)).getString(HOSTNAME_COL_NAME);

                String vcaDetailsName = createAndRetrieveVCADetailsName(row);

                VCenterConnectionProperties vcaProp = new VCenterConnectionProperties();
                vcaProp.setHostname(hostname);
                vcaProp.setVCenterAdapterDetailsName(vcaDetailsName);
                endpointToEndpointSpec.put(endpointName, vcaProp);
            }
        }
    }

    /**
     * construct row data for vcenteradapterdetails table from existing endpoint row for vcenter
     * endpoints
     *
     * @param row
     * @return
     * @throws SchemaAgreementException
     */
    public String createAndRetrieveVCADetailsName(Row row) throws SchemaAgreementException {
        final UDTValue vCenterConnectionProperties = ((UDTValue) row.getObject(VCENTER_CONNECTION_PROERPTIES_COL));
        final UDTValue vCenterAdapterProperties =
                (UDTValue) vCenterConnectionProperties.getObject(VCENTER_ADAPTER_PROERPTIES_UDT_COL);
        String vcAdapterURL = vCenterAdapterProperties.getString("vcadapterurl");
        String vcaUser = vCenterAdapterProperties.getString("username");
        String vcaPassword = vCenterAdapterProperties.getString("password");
        return addVCenterAdapterRow(vcAdapterURL, vcaUser, vcaPassword);
    }

    /**
     * Drop column vcenterconnectionproperties from endpoint table
     *
     * @throws SchemaAgreementException
     */
    public void removeVCenterConnectionPropertiesUDTFromEndpoint() throws SchemaAgreementException {
        SchemaStatement dropColumn =
                SchemaBuilder.alterTable(ENDPOINT_TABLE).dropColumn(VCENTER_CONNECTION_PROERPTIES_COL);
        executeWithSchemaAgreement(dropColumn);
    }

    /**
     * drop older UDT of type vcenterconnectionproperties, which had columns hostname and association
     * with UDT of type VCenterAdapterProperties
     *
     * @throws SchemaAgreementException
     */
    public void dropVCenterConnectionPropertiesUDTType() throws SchemaAgreementException {
        final Drop dropType = SchemaBuilder.dropType(VCENTER_CONNECTION_PROERPTIES_COL).ifExists();
        executeWithSchemaAgreement(dropType);
    }

    /**
     * add new UDT of type vcenterconnectionproperties, with the columns hostname and
     * vcenteradapterdetailsname
     *
     * @throws SchemaAgreementException
     */
    public void createNewVCenterConnectionPropertiesUDT() throws SchemaAgreementException {
        final CreateType createType = SchemaBuilder.createType(VCENTER_CONNECTION_PROERPTIES_COL)
                .addColumn(HOSTNAME_COL_NAME, text()).addColumn(VCENTER_ADAPTER_DETAILS_NAME_UDT_COL, text());
        executeWithSchemaAgreement(createType);
    }

    /**
     * add a column to endpoint table with new UDT created of type vcenterconnectionproperties
     *
     * @throws SchemaAgreementException
     */
    public void addNewVCenterConnectionPropertiesUDTColumnToEndpoint() throws SchemaAgreementException {
        SchemaStatement statement =
                SchemaBuilder.alterTable(ENDPOINT_TABLE).addColumn(VCENTER_CONNECTION_PROERPTIES_COL)
                        .udtType(SchemaBuilder.frozen(VCENTER_CONNECTION_PROERPTIES_COL));
        executeWithSchemaAgreement(statement);
    }

    /**
     * Update the endpoint table with the values for the column vcenterconnectionproperties associated
     * to new vcenter adapter details created
     *
     * @throws SchemaAgreementException
     */
    public void addNewUDTValuesToEndpoint() throws SchemaAgreementException {
        BoundStatement update;
        PreparedStatement updateEndpointStatement = session.prepare(PREPARED_UPDATE);
        UserType udtType =
                session.getCluster().getMetadata().getKeyspace(keyspace).getUserType(VCENTER_CONNECTION_PROERPTIES_COL);

        for (Map.Entry<String, VCenterConnectionProperties> entrySet : endpointToEndpointSpec.entrySet()) {
            VCenterConnectionProperties VCenterConnectionProperties = entrySet.getValue();
            UDTValue udtValue =
                    udtType.newValue().setString(HOSTNAME_COL_NAME, VCenterConnectionProperties.getHostname())
                            .setString(VCENTER_ADAPTER_DETAILS_NAME_UDT_COL,
                                    VCenterConnectionProperties.getVCenterAdapterDetailsName());
            do {
                update = updateEndpointStatement.bind(udtValue, entrySet.getKey());
                executeWithSchemaAgreement(update);
            } while (shouldRetry(entrySet.getKey()));
        }
    }

    /**
     * Adds row into vcenteradapter table
     *
     * @param vcAdapterURL
     * @param vcaUser
     * @param vcaPassword
     * @return
     * @throws SchemaAgreementException
     */
    public String addVCenterAdapterRow(String vcAdapterURL, String vcaUser, String vcaPassword)
            throws SchemaAgreementException {
        String id = UUID.randomUUID().toString();
        executeWithSchemaAgreement(vcAdapterAddRowPreparedStatement.bind(id, vcAdapterURL, vcaUser, vcaPassword));
        return id;
    }

    /**
     * check if upgrade script should proceed; true if there exists a column in UDT of type
     * vcenterconnectionproperties
     *
     * @return
     */
    public boolean proceedDBUpgrade() {
        return session.getCluster().getMetadata().getKeyspace(keyspace).getUserType(VCENTER_CONNECTION_PROERPTIES_COL)
                .getFieldNames().contains(VCENTER_ADAPTER_PROERPTIES_UDT_COL);
    }

    /**
     * check if value for vcenterconnectionproperties column exists in the given endpoint row
     *
     * @param row
     * @return
     */
    private boolean isVCColumnExists(Row row) {
        try {
            return !row.isNull(VCenterConnectionPropertiesUpgrade.VCENTER_CONNECTION_PROERPTIES_COL);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * check if update row on endpoint table should retried; true if value is null for
     * vcenterconnectionproperties column
     *
     * @param endpointName
     * @return
     */
    private boolean shouldRetry(String endpointName) {
        String simpleStatement = String
                .format("select name, vcenterconnectionproperties from endpointspec where name = '%s';", endpointName);
        ResultSet resultSet = session.execute(new SimpleStatement(simpleStatement));
        if (resultSet.iterator().hasNext()) {
            return resultSet.iterator().next().isNull(VCENTER_CONNECTION_PROERPTIES_COL);
        }
        return true;
    }

}