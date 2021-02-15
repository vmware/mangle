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

import javax.validation.constraints.NotEmpty;

import com.datastax.driver.core.DataType.Name;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.model.enums.DatabaseType;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * ConnectionProperties for Database.
 *
 * @author kumargautam
 */
@UserDefinedType("databaseConnectionProperties")
@ApiModel(description = "Database connection properties should be specified if endpoint type is DATABASE")
@Data
public class DatabaseConnectionProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Name of parent Endpoint which will be used in the fault apis")
    @NotEmpty
    private String parentEndpointName;

    @ApiModelProperty(value = "DatabaseType is an enum. please use appropriate value", hidden = true)
    @CassandraType(type = Name.VARCHAR)
    private DatabaseType dbType;

    @ApiModelProperty(hidden = true, value = "Parent EndpointType is an enum. please use appropriate value")
    @CassandraType(type = Name.VARCHAR)
    private EndpointType parentEndpointType;
}
