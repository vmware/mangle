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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.services.enums.AgentFaultName;

/**
 * Specification for DbTransactionErrorFault.
 *
 * @author kumargautam
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DbTransactionErrorFaultSpec extends DbFaultSpec {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Table name on which transaction should fail")
    @NotEmpty
    private String tableName;

    @ApiModelProperty(value = "Error code that should be thrown incase of transaction failure, refer get api : faults/db-transaction-error-code", example = "invalid_transaction_state")
    @NotEmpty
    private String errorCode;

    @ApiModelProperty(value = "What percentage of transaction queries should fail", example = "60")
    @NotNull
    @Min(1)
    @Max(100)
    private Integer percentage;

    @SuppressWarnings("squid:S2637")
    public DbTransactionErrorFaultSpec() {
        setFaultName(AgentFaultName.INJECT_DB_TRANSACTION_ERROR_FAULT.getValue());
        setSpecType(this.getClass().getName());
    }
}
