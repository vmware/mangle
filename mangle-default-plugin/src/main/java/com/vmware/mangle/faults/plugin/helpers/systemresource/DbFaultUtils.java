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

package com.vmware.mangle.faults.plugin.helpers.systemresource;

import static com.vmware.mangle.utils.constants.FaultConstants.DATABASE_CONNECTION_LEAK_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DATABASE_CONNECTION_LEAK_REMEDIATION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DATABASE_TRANSACTION_ERROR_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DATABASE_TRANSACTION_ERROR_REMEDIATION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DATABASE_TRANSACTION_LATENCY_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DATABASE_TRANSACTION_LATENCY_REMEDIATION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_ERROR_CODE_KEY;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_LATENCY_KEY;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_PERCENTAGE_KEY;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_SSL_ENABLED;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_TABLE_NAME_KEY;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.PASSWORD_KEY;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC;
import static com.vmware.mangle.utils.constants.FaultConstants.USERNAME;

import java.util.Map;

import lombok.experimental.UtilityClass;

import com.vmware.mangle.services.enums.FaultName;

/**
 * Utility methods for DbFault.
 *
 * @author kumargautam
 */
@UtilityClass
public class DbFaultUtils {

    public String getDbConnectionLeakInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return scriptBasePath + FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase()).getScriptFileName()
                + String.format(DATABASE_CONNECTION_LEAK_INJECTION_COMMAND_WITH_ARGS, faultArgs.get(DB_NAME),
                        faultArgs.get(USERNAME), faultArgs.get(PASSWORD_KEY), faultArgs.get(DB_PORT),
                        faultArgs.get(DB_SSL_ENABLED), faultArgs.get(TIMEOUT_IN_MILLI_SEC));
    }

    public String getDbConnectionLeakRemediationCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return scriptBasePath + FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase()).getScriptFileName()
                + String.format(DATABASE_CONNECTION_LEAK_REMEDIATION_COMMAND_WITH_ARGS, faultArgs.get(DB_NAME),
                        faultArgs.get(USERNAME), faultArgs.get(DB_SSL_ENABLED), faultArgs.get(DB_PORT));
    }

    public static String getDbTransactionErrorInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return scriptBasePath + FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase()).getScriptFileName()
                + String.format(DATABASE_TRANSACTION_ERROR_INJECTION_COMMAND_WITH_ARGS, faultArgs.get(DB_NAME),
                        faultArgs.get(USERNAME), faultArgs.get(PASSWORD_KEY), faultArgs.get(DB_PORT),
                        faultArgs.get(DB_TABLE_NAME_KEY), faultArgs.get(DB_ERROR_CODE_KEY),
                        faultArgs.get(DB_PERCENTAGE_KEY), faultArgs.get(DB_SSL_ENABLED),
                        faultArgs.get(TIMEOUT_IN_MILLI_SEC));
    }

    public static String getDbTransactionErrorRemediationCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return scriptBasePath + FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase()).getScriptFileName()
                + String.format(DATABASE_TRANSACTION_ERROR_REMEDIATION_COMMAND_WITH_ARGS, faultArgs.get(DB_NAME),
                        faultArgs.get(USERNAME), faultArgs.get(PASSWORD_KEY), faultArgs.get(DB_PORT),
                        faultArgs.get(DB_SSL_ENABLED), faultArgs.get(DB_TABLE_NAME_KEY));
    }

    public static String getDbTransactionLatencyInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return scriptBasePath + FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase()).getScriptFileName()
                + String.format(DATABASE_TRANSACTION_LATENCY_INJECTION_COMMAND_WITH_ARGS, faultArgs.get(DB_NAME),
                        faultArgs.get(USERNAME), faultArgs.get(PASSWORD_KEY), faultArgs.get(DB_PORT),
                        faultArgs.get(DB_TABLE_NAME_KEY), faultArgs.get(DB_LATENCY_KEY),
                        faultArgs.get(DB_PERCENTAGE_KEY), faultArgs.get(DB_SSL_ENABLED),
                        faultArgs.get(TIMEOUT_IN_MILLI_SEC));
    }

    public static String getDbTransactionLatencyRemediationCommand(Map<String, String> faultArgs,
            String scriptBasePath) {
        return scriptBasePath + FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase()).getScriptFileName()
                + String.format(DATABASE_TRANSACTION_LATENCY_REMEDIATION_COMMAND_WITH_ARGS, faultArgs.get(DB_NAME),
                        faultArgs.get(USERNAME), faultArgs.get(PASSWORD_KEY), faultArgs.get(DB_PORT),
                        faultArgs.get(DB_SSL_ENABLED), faultArgs.get(DB_TABLE_NAME_KEY));
    }
}