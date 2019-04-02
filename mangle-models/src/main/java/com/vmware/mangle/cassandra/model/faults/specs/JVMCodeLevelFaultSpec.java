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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author bkaranam
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class JVMCodeLevelFaultSpec extends JVMAgentFaultSpec {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "String value. Value should be the name of Target class for code "
            + "Injection", required = true, example = "lang.java.test")
    protected String className;
    @ApiModelProperty(value = "String value. Value should be One of the Rule Events Supported by Byteman."
            + " please refer rule-events section of byteman documentation for all the supported events", required = true, example = "AT ENTRY")
    protected String ruleEvent;
    @ApiModelProperty(value = "String value. Value should be the name of the Method with in the class targeted "
            + "for code Injection", required = true, example = "testMethod")
    protected String methodName;
}
