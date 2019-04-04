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

import com.vmware.mangle.services.enums.BytemanFaultType;

/**
 * @author bkaranam
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class JavaThrowExceptionFaultSpec extends JVMCodeLevelFaultSpec {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "String value. You can provide error message of the Exception to be thrown", required = true, example = "Exception in thread \"main\" java.lang.NullPointerException")
    private String exceptionMessage;
    @ApiModelProperty(value = "String value. You can provide Java Classname of the Exception to be thrown", required = true, example = "java.lang.NullPointerException")
    private String exceptionClass;

    public JavaThrowExceptionFaultSpec() {
        setFaultType(BytemanFaultType.EXCEPTION.toString());
        setFaultName(BytemanFaultType.EXCEPTION.toString());
        setSpecType(this.getClass().getName());
    }
}
