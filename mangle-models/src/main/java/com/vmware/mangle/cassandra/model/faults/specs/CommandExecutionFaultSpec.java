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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;

/**
 * @author bkaranam
 *
 *
 */
@Data
@ToString(exclude = { "injectionCommandInfoList", "remediationCommandInfoList", "testMachinePreperationCommandInfoList",
        "cleanUpCommandInfoList", "supportScriptInfo", "endpoint", "credentials" }, callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommandExecutionFaultSpec extends AutoRemediatedFaultSpec {
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    protected List<CommandInfo> injectionCommandInfoList;
    @JsonIgnore
    protected List<CommandInfo> remediationCommandInfoList;
    @JsonIgnore
    protected List<CommandInfo> testMachinePreperationCommandInfoList;
    @JsonIgnore
    protected List<CommandInfo> cleanUpCommandInfoList;
    @JsonIgnore
    protected List<SupportScriptInfo> supportScriptInfo;
    @JsonIgnore
    protected Map<String, String> args;
    @ApiModelProperty(value = "This Directory is Used by Mangle to copy fault invocation scripts", example = "/tmp/")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected String injectionHomeDir = "/tmp/";
    @JsonIgnore
    @Transient
    protected transient EndpointSpec endpoint;
    @JsonIgnore
    @Transient
    protected transient CredentialsSpec credentials;

    public String getInjectionHomeDir() {
        return injectionHomeDir;
    }

    public void setInjectionHomeDir(String injectionHomeDir) {
        if (!injectionHomeDir.endsWith("/")) {
            this.injectionHomeDir = injectionHomeDir + "/";
        } else {
            this.injectionHomeDir = injectionHomeDir;
        }
    }
}
