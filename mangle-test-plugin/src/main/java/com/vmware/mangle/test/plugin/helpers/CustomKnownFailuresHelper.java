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

package com.vmware.mangle.test.plugin.helpers;

import static com.vmware.mangle.utils.constants.FaultConstants.EXPECTED_MESSAGE_FOR_KILL_OPERATION_NOT_PERMITTED;
import static com.vmware.mangle.utils.constants.FaultConstants.MESSAGE_THROWN_FOR_KILL_OPERATION_NOT_PERMITTED;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_IOSIZE_GREATER_THAN_DISK_SIZE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_TARGET_DIRECTORY_DOESNT_HAVE_PERMISSION_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.EXPECTED_MESSAGE_FOR_FILE_NOT_FOUND;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.FAULT_ALREADY_REMEDIATED;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_ALREADY_REMEDIATED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_ALREADY_RUNNING_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_DIRECTORY_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_PARALLEL_EXECUTION_NOT_SUPPORTED_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_PRECHECK_FAILED_OUTPUT;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

/**
 * @author hkilari
 *
 */
@UtilityClass
public class CustomKnownFailuresHelper {

    public Map<String, String> getKnownFailuresOfSystemResourceFaultInjectionRequest() {
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_PRECHECK_FAILED_OUTPUT, null);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_ALREADY_RUNNING_OUTPUT,
                SYSTEM_RESOURCE_FAULT_PARALLEL_EXECUTION_NOT_SUPPORTED_MESSAGE);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_DIRECTORY_NOT_FOUND_OUTPUT, null);
        knownFailureMap.put(DISKIO_FAULT_IOSIZE_GREATER_THAN_DISK_SIZE_OUTPUT, null);
        knownFailureMap.put(DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT,
                DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT_MESSAGE);
        knownFailureMap.put(DISKIO_FAULT_TARGET_DIRECTORY_DOESNT_HAVE_PERMISSION_OUTPUT, null);
        knownFailureMap.put(MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_OUTPUT,
                MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_MESSAGE);
        knownFailureMap.put(EXPECTED_MESSAGE_FOR_KILL_OPERATION_NOT_PERMITTED,
                MESSAGE_THROWN_FOR_KILL_OPERATION_NOT_PERMITTED);
        knownFailureMap.put(KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_OUTPUT,
                KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_MESSAGE);
        knownFailureMap.put(KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_OUTPUT,
                KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_MESSAGE);

        return knownFailureMap;
    }

    public Map<String, String> getKnownFailuresOfSystemResourceFaultRemediationRequest() {
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put(EXPECTED_MESSAGE_FOR_FILE_NOT_FOUND, FAULT_ALREADY_REMEDIATED);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_PRECHECK_FAILED_OUTPUT, null);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_ALREADY_REMEDIATED_OUTPUT, FAULT_ALREADY_REMEDIATED);
        return knownFailureMap;
    }
}
