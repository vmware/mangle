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

package com.vmware.mangle.faults.plugin.helpers;

/**
 * @author hkilari
 *
 */
public class Messages {
    public static final String CONCURRENT_TASK_EXECUTION_FAILURE =
            "Another fault is being executed in test Machine. Please try after some time.";
    public static final String INPUT_VALIDATION_FAILURE = "Failed to validate Input";
    public static final String DISK_SPACE_FAULT_TASK_INPUT_VALIDATION_FAILURE =
            "TestMachine or Directory Path should not be Empty";
    public static final String SUCCESSFULY_TASK_CREATED = "Created Task Successfully";
    public static final String FAILED_TO_CREATE_REMEDIATION_TASK = "Failed to create Appropriate Remediation Task";
    public static final String NO_TASK_FOUND = "No Task Found with Given ID";
    public static final String NOT_A_INJECTION_TASK = "Given Task is not a Injection Task";
    public static final String INVALID_STATE_FOR_REMEDIATION =
            "The state of Task is not valid for Remediation. Task Status should be Completed for remediation to start";
    public static final String FAULT_ALREADY_REMEDIATED = "The Fault is already Remediated";
    public static final String DISK_SPACE_FAULT_DESCRIPTION = "Disk Fill up task to fill directory: %s on Machine: %s";
    public static final String DISK_SPACE_FAULT_REMEDIATION_DESCRIPTION =
            "Disk SpaceFault Remediation task to clear directory: %s on Machine: %s";
    public static final String DISK_IO_FAULT_DESCRIPTION =
            "Disk I/O Fault Injection Task for directory: %s on Machine: %s";
    public static final String DISK_IO_FAULT_REMEDIATION_DESCRIPTION =
            "Disk IO Fault Remediation task for directory: %s on Machine: %s";
    public static final String FAULT_REMEDIATION_NOT_SUPPORTED = "The injected fault do not support remediation";

    private Messages() {
    }
}
