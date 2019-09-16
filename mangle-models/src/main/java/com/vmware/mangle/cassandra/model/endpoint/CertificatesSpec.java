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

package com.vmware.mangle.cassandra.model.endpoint;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Certificates Spec Model
 *
 * @author bkaranam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class CertificatesSpec extends MangleDto {

    private static final long serialVersionUID = 1L;
    @Indexed
    private String id;
    @PrimaryKeyColumn(value = "name", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    @NotEmpty
    @Pattern(regexp = "^[A-Za-z0-9-_.]+$", message = "consists only alphanumeric with special characters (_ - .)")
    private String name;

    @JsonProperty(required = false)
    @ApiModelProperty(hidden = true)
    @CassandraType(type = Name.VARCHAR)
    @Indexed
    private EndpointType type;

    public CertificatesSpec() {
        this.id = super.generateId();
    }

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return this.name;
    }
}
