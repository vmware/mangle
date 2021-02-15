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

package com.vmware.mangle.cassandra.model.resiliencyscore;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.DataType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.model.constants.ResiliencyScoreConstant;

/**
 * @author dbhat
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class ServiceFamily extends MangleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(name = "id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    private String id;

    @ApiModelProperty(value = "Service family name")
    @Indexed
    @JsonProperty(defaultValue = ResiliencyScoreConstant.RESILIENCY_SCORE_DEFAULT_SERVICE_FAMILY_NAME)
    private String name;

    @ApiModelProperty(value = "Query/Alert conditions common to all the services under specified service family. The specified queries / alert conditions are run for each of the services defined.")
    @CassandraType(type = DataType.Name.UDT, userTypeName = "queryParameters")
    private List<QueryParameters> commonQueries;

    @ApiModelProperty("Tags in Metric Monitoring System common to all the services in the specified service family.")
    private Map<String, String> tags;

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return id;
    }

    /**
     * Setting this.id via calling generateId() of Super class.
     */
    public ServiceFamily() {
        this.id = super.generateId();
    }
}
