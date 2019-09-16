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

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * CertificatesSpec Reading Converter.
 *
 * @author bkaranam
 */
@ReadingConverter
@Log4j2
public class CertificatesSpecReadingConverter implements Converter<Row, CertificatesSpec> {
    private CassandraConverter cassandraConverter;

    public CertificatesSpecReadingConverter() {
        this.cassandraConverter = new MappingCassandraConverter();
    }

    public CertificatesSpecReadingConverter(MappingCassandraConverter mappingCassandraConverter) {
        this.cassandraConverter = mappingCassandraConverter;
    }

    @Override
    public CertificatesSpec convert(Row source) {
        log.debug("Start execution of convert() method...");
        EndpointType endpointType = EndpointType.valueOf(source.getString("type"));
        switch (endpointType) {
        case DOCKER:
            return (DockerCertificates) cassandraConverter.read(DockerCertificates.class, source);
        default:
            return (CertificatesSpec) cassandraConverter.read(CertificatesSpec.class, source);
        }
    }
}
