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

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.cassandra.model.MangleDto;


/**
 * @author dbhat
 */

@Table(value = "query_spec")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
@ApiModel(description = "QueryDto defines the query condition which can be executed in metric monitoring tool")
public class QueryDto extends MangleDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(value = "id", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    private String id;

    @ApiModelProperty(position = 1, value = "Name for the query definition. ")
    @NotEmpty
    @Indexed
    private String name;

    @ApiModelProperty(position = 2, value = "Weight to be applied while calculating the Resiliency score for the specified query")
    private float weight;

    @ApiModelProperty(position = 3, value = "Queries / alert conditions specific to the service. These queries will be used for calculating the resiliency score. The alert condition should return the time series data having 0 or 1 where 1 represents alert triggered and 0 otherwise")
    @NotEmpty
    private String queryCondition;

    @JsonIgnore
    @ApiModelProperty(position = 4, value = "Optional field and Default value is set to current time.")
    private Long lastUpdatedTime = System.currentTimeMillis();

    public QueryDto() {
        if (null == this.id) {
            this.id = super.generateId();
        }
    }

    @JsonIgnore
    @Override
    public String getPrimaryKey() {
        return this.id;
    }
}
