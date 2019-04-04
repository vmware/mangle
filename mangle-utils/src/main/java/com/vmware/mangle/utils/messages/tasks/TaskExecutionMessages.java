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

package com.vmware.mangle.utils.messages.tasks;


/**
 * @author hkilari
 *
 */
public class TaskExecutionMessages {
    private TaskExecutionMessages() {

    }

    public static final String CONCURRENT_TASK_EXECUTION_FAILURE =
            "Another fault is being executed in test Machine. Please try after some time.";
    public static final String INPUT_VALIDATION_FAILURE = "Failed to validate Input";
    public static final String DISK_SPACE_FAULT_TASK_INPUT_VALIDATION_FAILURE =
            "TestMachine or Directory Path should not be Empty";
    public static final String SUCCESSFULY_TASK_CREATED = "Created Task Successfully";
    public static final String FAILED_TO_CREATE_REMEDIATION_TASK = "Failed to create Appropriate Remediation Task";
    public static final String NO_TASK_FOUND = "No Task Found with Given ID";
    public static final String DISK_SPACE_FAULT_DESCRIPTION = "Disk Fill up task to fill directory: %s on Machine: %s";
    public static final String DISK_SPACE_FAULT_REMEDIATION_DESCRIPTION =
            "Disk SpaceFault Remediation task to clear directory: %s on Machine: %s";
    public static final String DISK_IO_FAULT_DESCRIPTION =
            "Disk I/O Fault Injection Task for directory: %s on Machine: %s";
    public static final String DISK_IO_FAULT_REMEDIATION_DESCRIPTION =
            "Disk IO Fault Remediation task for directory: %s on Machine: %s";
    public static final String FAULT_REMEDIATION_NOT_SUPPORTED = "The injected fault do not support remediation";
    //Messages required for TestExecutor Task
    public static final String CONCURRENT_TESTBED_USAGE_MESSAGE =
            "The requested testbed is currently in use, kindly choose a different test bed!";
    public static final String FAILED_EXECUTION_SUBMISSION_MESSAGE =
            "Failed to submit the execution, could not start the execution engine!";
    public static final String COMPLETION_FAILURE_MESSAGE = "Failed to complete the execution!";
    public static final String FAILED_EXECUTION_STATUS =
            "Execution is failed, could not persist reports and logs failed to send email!";
    public static final String TASK_EXECUTION_START_MESSAGE = "Starting Task with Name: %s";
    public static final String TASK_EXECUTION_FAILED_MESSAGE = "Task Execution Failed. Reason: ";
}
