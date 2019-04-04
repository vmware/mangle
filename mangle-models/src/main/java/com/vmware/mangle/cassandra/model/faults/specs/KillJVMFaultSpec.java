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

import com.datastax.driver.core.DataType.Name;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.services.enums.SysExitCodes;

/**
 * @author bkaranam
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class KillJVMFaultSpec extends JVMCodeLevelFaultSpec {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Enum value. Value should be one of the codes defined in SysExits", required = true, example = "OK")
    @CassandraType(type = Name.VARCHAR)
    private SysExitCodes exitCode;


    public KillJVMFaultSpec() {
        setFaultType(BytemanFaultType.KILL_JVM.toString());
        setFaultName(BytemanFaultType.KILL_JVM.toString());
        setSpecType(this.getClass().getName());
    }
}
