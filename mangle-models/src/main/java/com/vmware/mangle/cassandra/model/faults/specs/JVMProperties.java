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

package com.vmware.mangle.cassandra.model.faults.specs;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * @author bkaranam
 *
 *
 */
@UserDefinedType("JVMProperties")
@Data
public class JVMProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Java Process ID or JVM descriptor name", example = "1735 or app.jar")
    @JsonProperty(required = false)
    @NotEmpty
    private String jvmprocess;
    @ApiModelProperty(value = "JAVA_HOME Path to be set in Remote machine: Mandatory if jvmprocess is set", example = "/usr/java/latest")
    @JsonProperty(required = false)
    private String javaHomePath;
    @ApiModelProperty(value = "Username who started the java process")
    @JsonProperty(required = false)
    private String user;
    @ApiModelProperty(value = "Any free port which will be used for local socket connection from mangle agent to jvm process. "
            + "                Please make sure the user who started the jvm process have inbound and outbound localhost access to the port", example = "9091")
    @JsonProperty(required = false, defaultValue = "9091")
    private String port = "9091";
}
