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

package com.vmware.mangle.plugin.model.faults.specs;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModelProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.pf4j.Extension;

import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;


/**
 * Sample fault to demonstrate, how Mangle custom Fault Inputs can be Defined. The Fields provided
 * here will be requested as inputs by the custom fault during its execution. The Custom Fault
 * Developer needs to define an Implementation of PluginFaultSpec for each Custom Fault. The present
 * Implementation define two fileds 'field1', 'field2' as user Inputs for HelloMangleFault.
 *
 * @author jayasankarr
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
/**
 * Remove the annotation if setTimeoutInMilliseconds is enabled.
 */
@JsonIgnoreProperties({ "timeoutinMilliseconds" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
@Extension
public class HelloMangleFaultSpec extends PluginFaultSpec {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Field required for execution of Fault", example = "field1")
    private String field1;
    @NotEmpty
    @ApiModelProperty(value = "field2", example = "field2")
    private String field2;

    public HelloMangleFaultSpec() {
        setFaultName("HelloFault");
        setSpecType(this.getClass().getName());
    }

    /**
     * Enable the Field for specs associated with Mangle Custom Faults capable of self Remediation
     * post the user provided Timeout.
     */
    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
    }

}
