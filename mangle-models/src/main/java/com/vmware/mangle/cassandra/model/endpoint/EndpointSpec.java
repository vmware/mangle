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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
import lombok.ToString;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.cassandra.model.MangleDto;
import com.vmware.mangle.model.enums.EndpointGroupType;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Model class for Endpoint
 *
 * @author kumargautam
 */
@Data
@Table(value = "EndPointSpec")
@ToString(exclude = { "id" })
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class EndpointSpec extends MangleDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @Indexed
    private String id;
    @ApiModelProperty(position = 0, value = "Name of Endpoint which will be used in the fault apis")
    @PrimaryKeyColumn(value = "name", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    @NotEmpty
    @Pattern(regexp = "^[A-Za-z0-9-_.]+$", message = "consists only alphanumeric with special characters (_ - .)")
    private String name;
    @NotNull
    @ApiModelProperty(position = 1, value = "EndpointType is an enum. please use appropriate value")
    @Column
    @CassandraType(type = Name.VARCHAR)
    @Indexed
    private EndpointType endPointType;

    @ApiModelProperty(position = 2, value = "Name of credentials which is created using api /endpoints/credentials/<endpointType>. Can be ignored if credentials are not required")
    @JsonProperty(required = false)
    @Indexed
    private String credentialsName;

    @JsonIgnore
    @JsonProperty(required = false)
    @ApiModelProperty(position = 3)
    @Column
    @Valid
    @CassandraType(type = Name.UDT, userTypeName = "awsConnectionProperties")
    private AWSConnectionProperties awsConnectionProperties;

    @JsonIgnore
    @JsonProperty(required = false)
    @ApiModelProperty(position = 4)
    @Column
    @Valid
    @CassandraType(type = Name.UDT, userTypeName = "azureConnectionProperties")
    private AzureConnectionProperties azureConnectionProperties;
    @JsonProperty(required = false)
    @ApiModelProperty(position = 5)
    @CassandraType(type = Name.UDT, userTypeName = "dockerConnectionProperties")
    @Valid
    private DockerConnectionProperties dockerConnectionProperties;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 6)
    @CassandraType(type = Name.UDT, userTypeName = "remoteMachineConnectionProperties")
    @Valid
    private RemoteMachineConnectionProperties remoteMachineConnectionProperties;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 7)
    @CassandraType(type = Name.UDT, userTypeName = "k8sConnectionProperties")
    @Valid
    private K8SConnectionProperties k8sConnectionProperties;

    @ApiModelProperty(position = 8)
    @CassandraType(type = Name.UDT, userTypeName = "vCenterConnectionProperties")
    @Valid
    private VCenterConnectionProperties vCenterConnectionProperties;

    @ApiModelProperty(position = 9)
    @CassandraType(type = Name.UDT, userTypeName = "redisProxyConnectionProperties")
    @Valid
    private RedisProxyConnectionProperties redisProxyConnectionProperties;

    @ApiModelProperty(position = 10)
    @CassandraType(type = Name.UDT, userTypeName = "databaseConnectionProperties")
    @Valid
    private DatabaseConnectionProperties databaseConnectionProperties;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 11, value = "Tags to be associated while sending the metrics, events to Monitoring system. Format: key : value. Example Value is: \"environment\" : \"production\"")
    private Map<String, String> tags;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 12, value = "list of endpoint names of same types to form a group. Mandatory field if EndpointType is ENDPOINT_GROUP")
    private List<String> endpointNames;

    @ApiModelProperty(position = 13, value = "EndpointGroupType is an enum. please use appropriate value. Mandatory field if EndpointType is ENDPOINT_GROUP")
    @Column
    @CassandraType(type = Name.VARCHAR)
    private EndpointGroupType endpointGroupType;

    @JsonProperty(required = false)
    @ApiModelProperty(position = 14, value = "true or false , specify this option to disable fault injection on this endpoint", example = "true")
    private Boolean enable = true;

    @SuppressWarnings("squid:S2637")
    public EndpointSpec() {
        this.id = super.generateId();
    }

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return name;
    }
}
