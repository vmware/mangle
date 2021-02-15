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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.model.enums.DatabaseType;
import com.vmware.mangle.model.enums.EncryptField;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Credentials for Database.
 *
 * @author kumargautam
 */
@Table(value = "CredentialsSpec")
@Data
@EqualsAndHashCode(callSuper = true)
public class DatabaseCredentials extends CredentialsSpec {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "DatabaseType is an enum. please use appropriate value")
    @NotNull
    @CassandraType(type = Name.VARCHAR)
    private DatabaseType dbType;

    @ApiModelProperty(value = "Db User name to be used to make connection with database")
    @NotEmpty
    private String dbUserName;

    @ApiModelProperty(value = "Db password to be used to make connection with database")
    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @EncryptField
    private String dbPassword;

    @ApiModelProperty(value = "Db Port to be used to make connection with database", example = "5432")
    @JsonProperty(required = true, defaultValue = "5432")
    @NotNull
    @Min(0)
    @Max(65535)
    private Integer dbPort;

    @ApiModelProperty(value = "Db name to be used to make connection with database")
    @NotEmpty
    private String dbName;

    @NotNull
    @ApiModelProperty(value = "Specify if SSL enabled is true or false", example = "false")
    @JsonProperty(defaultValue = "false")
    private Boolean dbSSLEnabled = false;

    @SuppressWarnings("squid:S2637")
    public DatabaseCredentials() {
        setType(EndpointType.DATABASE);
    }
}
