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

package com.vmware.mangle.services.cassandra.converters;

import com.datastax.driver.core.Row;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.convert.ReadingConverter;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.AzureCredentials;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DatabaseCredentials;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * CredentialsSpec Reading Converter.
 *
 * @author kumargautam
 */
@ReadingConverter
@Log4j2
public class CredentialsSpecReadingConverter implements Converter<Row, CredentialsSpec> {
    private CassandraConverter cassandraConverter;

    public CredentialsSpecReadingConverter() {
        this.cassandraConverter = new MappingCassandraConverter();
    }

    public CredentialsSpecReadingConverter(MappingCassandraConverter mappingCassandraConverter) {
        this.cassandraConverter = mappingCassandraConverter;
    }

    @Override
    public CredentialsSpec convert(Row source) {
        log.debug("Start execution of convert() method...");
        EndpointType endpointType = EndpointType.valueOf(source.getString("type"));
        switch (endpointType) {
        case MACHINE:
            return (RemoteMachineCredentials) cassandraConverter.read(RemoteMachineCredentials.class, source);
        case AWS:
            return (AWSCredentials) cassandraConverter.read(AWSCredentials.class, source);
        case AZURE:
            return (AzureCredentials) cassandraConverter.read(AzureCredentials.class, source);
        case K8S_CLUSTER:
            return (K8SCredentials) cassandraConverter.read(K8SCredentials.class, source);
        case VCENTER:
            return (VCenterCredentials) cassandraConverter.read(VCenterCredentials.class, source);
        case DATABASE:
            return (DatabaseCredentials) cassandraConverter.read(DatabaseCredentials.class, source);
        default:
            return (CredentialsSpec) cassandraConverter.read(CredentialsSpec.class, source);
        }
    }
}
