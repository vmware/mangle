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

package com.vmware.mangle.test.plugin.constants;

import com.vmware.mangle.services.enums.FaultName;

/**
 * Add Constants Fields.
 *
 * @author kumargautam
 */
public class CommonConstants {

    private CommonConstants() {
    }

    public static final String ARGUEMENT_PREFIX = "__";
    public static final String OPERATION_INJECT = " --operation=inject ";
    public static final String PROCESS_IDENTIFIER_ARG = ARGUEMENT_PREFIX + "processIdentifier";
    public static final String KILL_PROCESS_REMEDIATION_COMMAND_ARG = ARGUEMENT_PREFIX + "remediationCommand";
    public static final String OS_TYPE_ARG = ARGUEMENT_PREFIX + "osType";
    public static final String KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.KILLPROCESSFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("--processIdentifier=\"%s\"").toString();
}
