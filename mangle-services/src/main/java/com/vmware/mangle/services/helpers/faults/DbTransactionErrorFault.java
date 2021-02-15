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

package com.vmware.mangle.services.helpers.faults;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.cassandra.model.endpoint.DatabaseCredentials;
import com.vmware.mangle.cassandra.model.faults.specs.DbTransactionErrorFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.helpers.DbTransactionErrorHelper;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 * @author kumargautam
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE, EndpointType.DOCKER, EndpointType.K8S_CLUSTER })
public class DbTransactionErrorFault extends AbstractFault {

    public DbTransactionErrorFault(DbTransactionErrorFaultSpec faultSpec) throws MangleException {
        super(faultSpec, TaskType.INJECTION);
        faultSpec.setFaultName(faultSpec.getFaultName() + "_"
                + ((DatabaseCredentials) faultSpec.getChildCredentials()).getDbType().toString().toLowerCase());
        validateDbTransactionErrorCode(faultSpec);
    }

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        DbTransactionErrorFaultSpec transactionErrorFaultSpec = (DbTransactionErrorFaultSpec) faultSpec;
        DatabaseCredentials databaseCredentials = (DatabaseCredentials) faultSpec.getChildCredentials();
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.DB_NAME_ARG, databaseCredentials.getDbName());
        specificArgs.put(FaultConstants.DB_USER_NAME_ARG, databaseCredentials.getDbUserName());
        specificArgs.put(FaultConstants.DB_PORT_ARG, String.valueOf(databaseCredentials.getDbPort()));
        specificArgs.put(FaultConstants.DB_PASSWORD_ARG, databaseCredentials.getDbPassword());
        specificArgs.put(FaultConstants.DB_TYPE_ARG, databaseCredentials.getDbType().toString());
        specificArgs.put(FaultConstants.DB_SSL_ENABLED_ARG, String.valueOf(databaseCredentials.getDbSSLEnabled()));
        specificArgs.put(FaultConstants.DB_TABLE_NAME_ARG, transactionErrorFaultSpec.getTableName());
        specificArgs.put(FaultConstants.DB_ERROR_CODE_ARG,
                DbTransactionErrorHelper.getDbTransactionErrorCodes(databaseCredentials.getDbType())
                        .get(transactionErrorFaultSpec.getErrorCode()));
        specificArgs.put(FaultConstants.DB_PERCENTAGE_ARG, String.valueOf(transactionErrorFaultSpec.getPercentage()));
        return specificArgs;
    }

    private void validateDbTransactionErrorCode(DbTransactionErrorFaultSpec faultSpec) throws MangleException {
        Map<String, String> dbTransactionErrorCodes = DbTransactionErrorHelper
                .getDbTransactionErrorCodes(((DatabaseCredentials) faultSpec.getChildCredentials()).getDbType());
        if (!dbTransactionErrorCodes.containsKey(faultSpec.getErrorCode())) {
            throw new MangleException(CommonConstants.DB_TRANSACTION_ERROR_CODE_INVALID, ErrorCode.GENERIC_ERROR);
        }
    }
}
