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

package com.vmware.mangle.cassandra.model.metricprovider;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.model.enums.MetricProviderType;

/**
 * Model Class for Metric Provider.
 *
 * @author ashrimali
 * @author dbhat
 *
 */

@Table(value = "metricprovider")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class MetricProviderSpec extends MangleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Indexed
    private String id;

    @ApiModelProperty(position = 0, value = "Name of MetricProvider which will be used to send metrics")
    @PrimaryKey(value = "name")
    @NotEmpty
    @Pattern(regexp = "^[A-Za-z0-9-_.]+$", message = "consists only alphanumeric with special characters (_ - .)")
    private String name;

    @NotNull
    @ApiModelProperty(position = 1, value = "Type of Metric Provider")
    private MetricProviderType metricProviderType;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 2)
    @CassandraType(type = Name.UDT, userTypeName = "waveFrontConnectionProperties")
    @Valid
    private WaveFrontConnectionProperties waveFrontConnectionProperties;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 3)
    @CassandraType(type = Name.UDT, userTypeName = "datadogConnectionProperties")
    @Valid
    private DatadogConnectionProperties datadogConnectionProperties;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 4)
    @CassandraType(type = Name.UDT, userTypeName = "dynatraceConnectionProperties")
    @Valid
    private DynatraceConnectionProperties dynatraceConnectionProperties;

    /**
     * Setting this.id via calling generateId() of Super class.
     */
    public MetricProviderSpec() {
        this.id = super.generateId();
    }

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return name;
    }

}