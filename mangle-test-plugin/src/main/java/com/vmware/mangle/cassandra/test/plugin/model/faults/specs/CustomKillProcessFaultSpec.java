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

package com.vmware.mangle.cassandra.test.plugin.model.faults.specs;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.pf4j.Extension;

import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * @author jayasankarr
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "timeoutinMilliseconds" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
@Extension
public class CustomKillProcessFaultSpec extends PluginFaultSpec {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Command to restart the process,if provided fault will be remediated", example = "if its a service: service restart command has to be provided")
    private String remediationCommand;
    @NotEmpty
    @ApiModelProperty(value = "A unique identifier for filtering process id.", example = "/var/lib/vcac")
    private String processIdentifier;

    public CustomKillProcessFaultSpec() {
        setFaultName(AgentFaultName.INJECT_KILL_PROCESS_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
    }

}
